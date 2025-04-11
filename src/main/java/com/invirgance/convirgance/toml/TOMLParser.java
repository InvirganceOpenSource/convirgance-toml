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
    
    public TOMLParser(BufferedReader reader)
    {
        this.reader = reader;
    }
    
    
    private JSONObject parseObject() throws Exception
    {
        buffer = new StringBuilder();
        buffer.append("{");
        
        
        while ((line = reader.readLine()) != null)
        {
            line = line.trim();
            
            // ignore TOML Comment line 
            if (line.startsWith("#") || line.isEmpty()) continue;
            
            
            // Table name
            
                    
            // Key value pair
            if (line.contains("=")) parseKeyValue();
                    
                    
            // multi-line comment
        }
    }
    
    
    private void parseKeyValue() throws Exception
    {
        String[] tokens = line.split("=", 2);
        String key = parseKey(tokens[0].trim());
        
        buffer.append("\"");
        buffer.append("\"");
        buffer.append(key);
        buffer.append("\": ");
        
        
        Object value = parseValue(tokens[1].trim());
        
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
        
        // then work with the rest of the value
    }
    
    
    private String trimComment(String value)
    {
            
    }
    
    
    
    
    @Override
    public void close() throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    
    
}
