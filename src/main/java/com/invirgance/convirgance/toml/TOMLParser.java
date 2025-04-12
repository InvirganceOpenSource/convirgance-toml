/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.invirgance.convirgance.toml;

import com.invirgance.convirgance.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 *
 * @author timur
 */
public class TOMLParser implements AutoCloseable
{
    private BufferedReader reader;
    private String line;
    private StringBuilder buffer;
    private boolean first;
    
    public TOMLParser(BufferedReader reader)
    {
        this.reader = reader;
    }
    
    
    private JSONObject parseObject() throws Exception
    {
        first = true;
        buffer = new StringBuilder();
        buffer.append("{");
        
        while ((line = reader.readLine()) != null)
        {
            line = line.trim();
            
            // ignore TOML Comment line 
            if (line.startsWith("#") || line.isEmpty()) continue;
            
            
            // TODO: Table name logic
 
            // Key value pair
            if (line.contains("=")) 
            {
                // separate from previously written key-value pair if not first
                if (!first) buffer.append(",");
                parseKeyValue();
                first = false;
            }  
        }
        
        buffer.append("}");
        
        return new JSONObject(buffer.toString());
    }
    
    
    private void parseKeyValue() throws Exception
    {
        String[] tokens = line.split("=", 2); // this assumes there is no '=' in the key
        String key = parseKey(tokens[0].trim());
        Object value = parseValue(tokens[1].trim());
        
        buffer.append("\"");
        buffer.append(key);
        buffer.append("\": ");
        buffer.append(value.toString()); 
    }
    
    
    private String parseKey(String key) throws Exception
    {
        // empty key not allowed
        if (key.isBlank()) throw new Exception("Invalid TOML key");
        
        // quoted 
        if (isQuoted(key))
        {
            return key.substring(1, key.length()-1); // remove quotes
        }
        
        // bare
        if (isBare(key)) 
        {
            return key;
        }

        
        // TODO: implement dotted key case
        
        
        // Invalid Key Format
        throw new Exception("Invalid TOML Key Format: " + key);
    }
    
    
    private boolean isQuoted(String key)
    {
        return ((key.startsWith("\"") && key.endsWith("\"")) || (key.startsWith("'") && key.endsWith("'")));
    }
    
//    private boolean isDotted(String key)
//    {
//        
//    }

    private boolean isBare(String key)
    {
        return (key.matches("^[a-zA-Z0-9_-]+$"));
    }
    
    
    private Object parseValue(String value)
    {
        // first trim the comment at the end of the line if one exists
        if (value.contains("#"))
        {
            value = trimComment(value);
        }
        
        // TODO: lots of logic here, value can be many things including other 
        
        return value;
    }
    
    
    
    private String trimComment(String value)
    {
       return value; // TODO: remove comment at the end     
    }
    
    
    
    
    @Override
    public void close() throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    
    
}
