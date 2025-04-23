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
        String value;


        // will need a while loop until no key-value pairs are left
        key = parseKey();
        value = parseValue();
        object.put(key, value);


        
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

        // remove opening quote
        line = line.substring(1);


        // iterate over the line
        for (int i = 0; i < line.length(); i++)
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
        

        // remove opening quote
        line = line.substring(1);

        // iterate over the line
        for (int i = 0; i < line.length(); i++)
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

        // iterate over the line
        for (int i = 0; i < line.length(); i++)
        {
            c = line.charAt(i);
            if (c == '=' || c == ' ' || c == '\t')
            {
                line = line.substring(i+1);
                break;
            }
            key.append(c);
        }
        return key.toString();
    }
   
    // might need level structure
    private String parseDottedKey()
    {
        StringBuilder key = new StringBuilder();
        char c;

        // iterate over the line
        for (int i = 0; i < line.length(); i++)
        {
            c = line.charAt(i);
            if (c == '.')
            {
                line = line.substring(i+1);
                break;
            }
            key.append(c);
        }
        return key.toString();
    }

    // PROBLEM can be dotted/quoted inside the brackets, need structure for this, remembering the previous keys and their levels
    private String parseArrayKey()
    {
        StringBuilder key = new StringBuilder();
        char c;

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

    // PROBLEM can be dotted/quoted inside the brackets, need structure for this, remembering the previous keys and their levels
    private String parseTableKey()
    {
        StringBuilder key = new StringBuilder();
        char c;

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



    


    private String parseValue()
    {
        return "fake value";
    }
    
    // private Object parseValue(String value)
    // {
    //     // first trim the comment at the end of the line if one exists
    //     if (value.contains("#"))
    //     {
    //         value = trimComment(value);
    //     }
        
    //     // TODO: lots of logic here, value can be many things including other 
        
    //     return value;
    // }





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

//            throw new Exception("Invalid TOML Key Format: " + line);
        }

                
        }

        if (whitespace)
        {
            while (i < line.length() && (line.charAt(i) == ' ' || line.charAt(i) == '\t'))
            {
                i++;
            }
            
            if (line.charAt(i) == '=')
            {
                return true;
            }
        }

        
        return false;
    }

    private boolean isDotted(String line)
    {
       // TODO: 
       return false;
    }


    private boolean isAllowed(char c)
    {
        return (c >= 'a' && c <= 'z') || 
               (c >= 'A' && c <= 'Z') || 
               (c >= '0' && c <= '9') || 
               c == '_' || c == '-';
    }
    
    


    


    @Override
    public void close() throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    
    
}
