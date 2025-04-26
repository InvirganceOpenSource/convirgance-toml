/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.invirgance.convirgance.toml;

import com.invirgance.convirgance.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.function.ToDoubleBiFunction;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;


/**
 *
 * @author timur
 */
public class TOMLParser implements AutoCloseable
{
    private String mode;
    private BufferedReader reader;
    private String line;
    private StringBuilder buffer;
    private HashMap<String, List<String>> arrays;
    private List<String> tables;
    private String keyType;
//    private boolean first;
    

    
    public TOMLParser(BufferedReader reader)
    {
        this.reader = reader;
    }
    

    public JSONObject parseObject() throws Exception
    {
        return parseNestedObject(0);
    }
    
    private JSONObject parseNestedObject(int level) throws Exception
    {
        JSONObject object = new JSONObject(true);
        int currentLevel = level;
        String key;
        Object value;


        // will need a while loop until no key-value pairs are left
        key = parseKey();
        value = parseValue();

        System.out.println("key: " + key);
        System.out.println("value: " + value);


        // figure out the type of the value
        if (value instanceof JSONObject)
        {
            object.put(key, value);            
        }
        else
        {
            value = (String) value;

            // check if value is an integer / float / boolean
            if (((String)value).matches("\\d+"))  
            {
                object.put(key, Integer.parseInt((String)value));
            }
            else if (((String)value).matches("\\d+\\.\\d+"))
            {
                object.put(key, Float.parseFloat((String)value));
            }
            else if (((String)value).matches("true|false"))
            {
                object.put(key, Boolean.parseBoolean((String)value));
            }
            // local date WORKS
            else if (((String)value).matches("\\d{4}-\\d{2}-\\d{2}"))
            {
                object.put(key, LocalDate.parse((String)value));
            }
            // local time with optional fractional seconds WORKS
            else if (((String)value).matches("\\d{2}:\\d{2}:\\d{2}") || ((String)value).matches("\\d{2}:\\d{2}:\\d{2}.\\d+"))
            {
                object.put(key, LocalTime.parse((String)value));
            }
            // local date time with optional fractional seconds T or space  WORKS
            else if (((String)value).matches("\\d{4}-\\d{2}-\\d{2}[T\\s]\\d{2}:\\d{2}:\\d{2}") || ((String)value).matches("\\d{4}-\\d{2}-\\d{2}[T\\s]\\d{2}:\\d{2}:\\d{2}\\.\\d+"))
            {
                // ensure the separator is a T instead of a space
                value = ((String)value).replace(" ", "T");
                object.put(key, LocalDateTime.parse((String)value));
            }
            else
            {
                object.put(key, value);
            }
        }

        System.out.println("date-time: " + object.get("date-time").getClass());

        return object;
    }
    


    private String parseKey() throws Exception
    {
        // find non empty, non comment line
        while (line == null || line.isBlank() || line.startsWith("#"))
        {
            line = reader.readLine();
        }

        line = line.trim(); // is this needed?

        //parse key
        if (isArray(line))
        {
System.out.println("array");
            return parseArrayKey();
        }  
        else if (isTable(line))
        {
System.out.println("table");
            return parseTableKey();
        }
        else if (isQuoted(line))
        {
System.out.println("quoted key");
            return parseQuotedKey();
        }
        else if (isSingleQuoted(line))
        {
System.out.println("single quoted key");
            return parseSingleQuotedKey();
        }
        else if (isBare(line))
        {
System.out.println("bare key");
            return parseBareKey();
        }
        else if (isDotted(line))
        {
System.out.println("dotted key");
            return parseDottedKey();
        }
        else
        {
            throw new Exception("Invalid TOML Key Format: " + line);
        }
    }  





    private String parseQuotedKey()
    {
        StringBuilder key = new StringBuilder();
        boolean escaped = false;
        char c;
        int i;

        // remove opening quote
        line = line.substring(1);


        // iterate over the line
        for (i = 0; i < line.length(); i++)
        {
            c = line.charAt(i);

            // handle escaped characters without appending
            if (c == '\\' && !escaped) 
            {
                escaped = true;
                continue;
            }
            // handle closing quote
            else if (c == '"' && !escaped) 
            {
                line = line.substring(i+1);
                break;
            }

            // append character
            if (escaped){
                key.append("\\");
            }
            key.append(c);
            escaped = false;
        }

        return key.toString();
    }



    private String parseSingleQuotedKey()
    {
        StringBuilder key = new StringBuilder();
        boolean escaped = false;
        char c;
        int i;
        

        // remove opening quote
        line = line.substring(1);

        // iterate over the line
        for (i = 0; i < line.length(); i++)
        {
            c = line.charAt(i);

            // handle escaped characters without appending
            if (c == '\\' && !escaped) 
            {
                escaped = true;
                continue;
            }
            // handle closing quote
            else if (c == '\'' && !escaped) 
            {   
                // remove closing quote from rest of line
                line = line.substring(i+1);
                break;
            }

            // append character
            if (escaped){
                key.append("\\");
            }
            key.append(c);
            escaped = false;
        }

        return key.toString();    
    }

    
    private String parseBareKey()
    {
        StringBuilder key = new StringBuilder();
        char c;
        int i;

        // iterate over the line
        for (i = 0; i < line.length(); i++)
        {
            c = line.charAt(i);
            if (c == '=' || c == ' ' || c == '\t')
            {
                line = line.substring(i);
                break;
            }
            key.append(c);
        }
        return key.toString();
    }
   
    // might need level structure
    private String parseDottedKey()
    {
        String key;

        // get the first part of the line until the dot
        key = line.substring(0, line.indexOf("."));
        
        // move line past the dot
        line = line.substring(line.indexOf(".")+1);

        return key.trim();
    }

    // PROBLEM can be dotted/quoted inside the brackets, need structure for this, remembering the previous keys and their levels
    private String parseArrayKey()
    {
        StringBuilder key = new StringBuilder();
        char c;

        // remove the brackets
        line = line.substring(2);

        // iterate over the line
        for (int i = 0; i < line.length(); i++)
        {
            c = line.charAt(i);
            if (c == ']')
            {
                line = line.substring(i+2);
                break;
            }
            key.append(c);
        }

        return key.toString();
    }

    // PROBLEM can be dotted/quoted inside the brackets, need structure for this, remembering the previous keys and their levels
    private String parseTableKey()
    {
        StringBuilder key = new StringBuilder();
        char c;

        // remove the brackets
        line = line.substring(1);

        // iterate over the line
        for (int i = 0; i < line.length(); i++)
        {
            c = line.charAt(i);
            if (c == ']')
            {
                line = line.substring(i+1);
                break;
            }
            key.append(c);
        }
        return key.toString();
    }



    


    private Object parseValue() throws Exception
    {
        // skip over whitespace characters
        skipWhitespace();

        // if first character is '=', we have a simple, non-nested value
        if (line.charAt(0) == '=')
        {
            // move past the equals sign and skip over whitespace characters
            line = line.substring(1);
            skipWhitespace();


            // if the value is a string or array or inline table, we need to handle the escaped characters
            if (line.charAt(0) == '"')
            {
                return parseQuotedKey();
            }
            // array
            else if (line.charAt(0) == '[') 
            {
                return parseTableKey();
            }
            // inline table
            else if (line.charAt(0) == '{') 
            {
                throw new Exception("Nested tables are not yet supported");
            }
            else if (line.contains("#"))
            {
                return line.substring(0, line.indexOf("#")).trim();
            }
            else
            {
                return line.trim();
            }
        }
        else
        {
            return parseNestedObject(1);
        }
    }
    









    private boolean isQuoted(String line)
    {
        return (line.startsWith("\""));
    }

    private boolean isSingleQuoted(String line)
    {
        return (line.startsWith("'"));
    }

    private boolean isArray(String line)
    {
        return (line.startsWith("[["));
    }   

    private boolean isTable(String line)
    {
        return (line.startsWith("["));
    }


    // TDDO Double check this code
    private boolean isBare(String line)
    {
        char c;
        boolean whitespace = false;
        boolean bare = false;
        int i;
        

        // Check each character in the key
        for (i = 0; i < line.length(); i++) 
        {
            c = line.charAt(i);

            if ((c == ' ' || c == '\t')) 
            {
                if (!bare) continue; // skips through preceding whitespace

                whitespace = true;  // sets flag for whitespace and breaks out of loop
                break;  
            }
            else if (c == '=' && bare)
            {   
                return true;
            }
            else if (isAllowed(c))
            {
                bare = true;
            }
            else if (!isAllowed(c))
            {
                return false;
            }
        }

        if (whitespace)
        {

            while (i < line.length() && (line.charAt(i) == ' ' || line.charAt(i) == '\t'))
            {
                i++;
            }


            if (i < line.length() && line.charAt(i) == '=')
            {
                return true;
            }
            else
            {
                return false;
            }
        }

        return false;
    }

    private boolean isDotted(String line)
    {
        char c;
        boolean foundDot = false;
        boolean foundValidChar = false;
        int i = 0;
        
        // Skip leading whitespace
        while (i < line.length() && (line.charAt(i) == ' ' || line.charAt(i) == '\t')) 
        {
            i++;
        }
        
        // Check each character until we find an equals sign or invalid character
        while (i < line.length()) 
        {
            c = line.charAt(i);
            
            if (c == '=') 
            {
                // If we found a dot and at least one valid character, it's a dotted key
                return foundDot && foundValidChar;
            }
            
            if (c == '.') 
            {
                // If we find a dot after a valid character, mark it
                if (foundValidChar) {
                    foundDot = true;
                } else {
                    // Dot at the start or after another dot is invalid
                    return false;
                }
            } else if (isAllowed(c)) 
            {
                foundValidChar = true;
            } else if (c != ' ' && c != '\t') {
                // Found an invalid character
                return false;
            }
            
            i++;
        }
        
        return foundDot && foundValidChar;
    }

    // Checks if the character is an ASCII letter, number, underscore, or dash
    private boolean isAllowed(char c)
    {
        return (c >= 'a' && c <= 'z') || 
               (c >= 'A' && c <= 'Z') || 
               (c >= '0' && c <= '9') || 
               c == '_' || c == '-';
    }
    
    
    private void skipWhitespace()
    {
        while (line.charAt(0) == ' ' || line.charAt(0) == '\t')
        {
            line = line.substring(1);
        }
    }



    @Override
    public void close() throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    
    
}
