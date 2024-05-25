package com.example.blpslab1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.jcr.Node;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequest implements Serializable {
    private String username;
    private String targetDir;
    private ArrayList<MessageNode> list;



}