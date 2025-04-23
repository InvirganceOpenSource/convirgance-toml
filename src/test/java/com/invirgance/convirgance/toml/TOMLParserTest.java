/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.invirgance.convirgance.toml;

import com.invirgance.convirgance.json.JSONObject;
import com.invirgance.convirgance.source.ClasspathSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author timur
 */
public class TOMLParserTest
{
    @Test
    public void test1() throws Exception
    {
        ClasspathSource source = new ClasspathSource("/toml1.toml");
        BufferedReader reader = new BufferedReader(new InputStreamReader(source.getInputStream(), "UTF-8"));
        TOMLParser parser = new TOMLParser(reader);
        
        JSONObject object = parser.parseObject();

        System.out.println(object.toString());
    }

    
    @Test
    public void test2() throws Exception
    {
        ClasspathSource source = new ClasspathSource("/toml2.toml");
        BufferedReader reader = new BufferedReader(new InputStreamReader(source.getInputStream(), "UTF-8"));
        TOMLParser parser = new TOMLParser(reader);
        
        JSONObject object = parser.parseObject();

        System.out.println(object.toString());
    }

    
    @Test
    public void test3() throws Exception
    {
        ClasspathSource source = new ClasspathSource("/toml3.toml");
        BufferedReader reader = new BufferedReader(new InputStreamReader(source.getInputStream(), "UTF-8"));
        TOMLParser parser = new TOMLParser(reader);
        
        JSONObject object = parser.parseObject();

        System.out.println(object.toString());
    }

    
    @Test
    public void test4() throws Exception
    {
        ClasspathSource source = new ClasspathSource("/toml4.toml");
        BufferedReader reader = new BufferedReader(new InputStreamReader(source.getInputStream(), "UTF-8"));
        TOMLParser parser = new TOMLParser(reader);
        
        JSONObject object = parser.parseObject();

        System.out.println(object.toString());
    }

    
    @Test
    public void test5() throws Exception
    {
        ClasspathSource source = new ClasspathSource("/toml5.toml");
        BufferedReader reader = new BufferedReader(new InputStreamReader(source.getInputStream(), "UTF-8"));
        TOMLParser parser = new TOMLParser(reader);
        
        JSONObject object = parser.parseObject();

        System.out.println(object.toString());
    }

    
}
