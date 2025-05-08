/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.invirgance.convirgance.toml;

import com.invirgance.convirgance.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;




/**
 *
 * @author timur
 */
public class TOMLParser implements AutoCloseable
{
    private BufferedReader reader;
    private String line;
    // used to store the keys hierarchy
    private ArrayList<String> tables = new ArrayList<String>();
    private boolean foundTable;

    

    
    public TOMLParser(BufferedReader reader) throws IOException 
    {
        this.reader = reader;
        this.line = reader.readLine();
    }
    

    public JSONObject parseObject() throws Exception
    {
        JSONObject object = new JSONObject(true);
        ArrayList<String> keys;

        
        while ((keys = parseKeys()) != null && keys.size() > 0)
        {   
System.out.println("returned keys: " + keys);
            if (foundTable)
            {
                foundTable = false;
                tables = keys;
                // print tables
                System.out.println("tables: " + tables);
                continue;

            }

            setValue(object, keys, parseValue());

        }

        return object;
    }




    private void setValue(JSONObject record, ArrayList<String> keys, Object value)
    {
        System.out.println("setting value: " + value);
        //  first parse through the table hierarchy
        for(int i=0; i<tables.size(); i++)
        {
            if(!record.containsKey(tables.get(i))) record.put(tables.get(i), new JSONObject());

            record = record.getJSONObject(tables.get(i));
        }

        // then parse through the key hierarchy
        for(int i=0; i<keys.size()-1; i++)
        {
            if(!record.containsKey(keys.get(i))) record.put(keys.get(i), new JSONObject());

            record = record.getJSONObject(keys.get(i));
        }

        // finally set the value
        System.out.println("keys: " + keys);
        record.put(keys.get(keys.size()-1), value);
    }




    private ArrayList<String> parseKeys() throws Exception
    {
        ArrayList<String> keys = new ArrayList<String>();

        // find non empty, non comment line
        while (line != null && (line.isBlank() || line.startsWith("#")))
        {
            line = reader.readLine();
        }

        if (line == null) return null;
        
        line = line.trim(); // is this okay?

        if (line.startsWith("[["))
        {
            // TODO: how to handle arrays?
            keys.add(parseArrayKey());
            return keys;
        }
        else if (line.startsWith("["))
        {
            foundTable = true;
            line = line.substring(1);
        }


        while (line != null && !line.isBlank() && !line.startsWith("=") && !line.startsWith("#"))
        {
            // remove the closing brackets left after tables/arrays
            if (line.startsWith("]"))
            {
                line = line.substring(1);
                break;
            }

        

            
            skipWhitespace();

            if (isQuoted(line))
            {
System.out.println("quoted key");
                keys.add(parseQuotedKey());
            }
            else if (isSingleQuoted(line))
            {
System.out.println("single quoted key");
                keys.add(parseSingleQuotedKey());
            }
            else if (isDotted(line))
            {
System.out.println("dotted key");
                keys.add(parseDottedKey());
            }
            else if (isBare(line))
            {
System.out.println("bare key");
                keys.add(parseBareKey());
            }
            else
            {
                throw new Exception("Invalid TOML Key Format: " + line);
            } 
            line = line.trim();
        }

        System.out.println("returning keys: " + keys);

        return keys;

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

// System.out.println("parsing bare key: " + line);

        skipWhitespace();
        while (line != null && !line.isBlank())
        {
            c = line.charAt(0);
            if (isAllowed(c))
            {
                key.append(c);
                line = line.substring(1);
            }
            else
            {
                break;
            }

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




    

    




    private Object parseValue() throws Exception
    {
        String value;

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
            // // array
            // else if (line.charAt(0) == '[') 
            // {
            //     return parseTableKey();
            // }
            // inline table
            else if (line.charAt(0) == '{') 
            {
                throw new Exception("Nested tables are not yet supported");
            }
            else if (line.contains("#"))
            {
                // record the value before the comment
                value = line.substring(0, line.indexOf("#")).trim();
                // move past the comment
                line = line.substring(line.indexOf("#")+1);
                return value;
            }
            else
            {
                value = line.trim();
                line = reader.readLine();
                return value;
            }
        }
        else
        {
            return parseObject();
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






    // TODO Double check this code
    private boolean isBare(String line)
    {
//System.out.println("found bare: " + line);
        char c;
        boolean whitespace = false;
        boolean bare = false;
        int i;
        

        skipWhitespace();

        // Check each character in the key
        for (i = 0; i < line.length(); i++) 
        {
            c = line.charAt(i);


            if ((c == '=' || c == ' ' || c == '\t')  && bare)
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

        return bare;
    }

    private boolean isDotted(String line)
    {
        char c;
        boolean foundValidChar = false;
        int i = 0;
        
        
        // Check each character until we find an equals sign or invalid character
        while (i < line.length()) 
        {
            c = line.charAt(i);
                 
            if (c == '.') 
            {
                // If we find a dot after a valid character, mark it
                return foundValidChar;
            } else if (isAllowed(c)) 
            {
                foundValidChar = true;
            } else if (c != ' ' && c != '\t') {
                // Found an invalid character
                return false;
            }
            
            i++;
        }
        
        return false;
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
        while ((!line.isBlank()) && (line.charAt(0) == ' ' || line.charAt(0) == '\t'))
        {
            line = line.substring(1);
        }

        if (line.isBlank())
        {
            try {
                line = reader.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            skipWhitespace();
        }
    }






    @Override
    public void close() throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
