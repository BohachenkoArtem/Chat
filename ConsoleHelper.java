package com.javarush.task.task30.task3008;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleHelper {
   private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

   public static void writeMessage(String message){
       System.out.println(message);
   }

   public static String readString(){
       boolean isCorrectString=false;
       String message=null;
       while (!isCorrectString){
           try {
               message=reader.readLine();
               isCorrectString=true;
           } catch (IOException e) {
               System.out.println("Произошла ошибка при попытке ввода текста. Попробуйте еще раз.");
           }
               }
       return message;
   }

   public static int readInt(){
       boolean isCorrectNumber=false;
       int number = 0;
       while (!isCorrectNumber){
          try{ number = Integer.parseInt(readString());
              isCorrectNumber=true;
          } catch (NumberFormatException e){
              System.out.println("Произошла ошибка при попытке ввода числа. Попробуйте еще раз.");
          }
       }
return number;
   }
}
