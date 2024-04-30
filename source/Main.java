package source;

import source.dataStructure.MyBuffer;
import source.entity.Cat;
import source.generators.CatGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        File catFile = new File("cats");
        if(catFile.exists()){
            catFile.delete();
        }
        MyBuffer<Cat> myBuffer = new MyBuffer<>(1000);
        List<Cat> catList = new ArrayList<>();
        for(int i = 0; i < 15100; i++){
            Cat cat = CatGenerator.generateCat();
            catList.add(cat);
        }

        Cat writtenCat = catList.get(1001);

        try {
            myBuffer.writeCatsToFile(catList, catFile);
        } catch (Exception e){
            throw new RuntimeException(e);
        }

        List<Cat> restoredCats = new ArrayList<>();
        try {
            restoredCats = myBuffer.readCatsFromFile(catFile);
        } catch (Exception e){
            throw new RuntimeException(e);
        }

        Cat restoredCat = restoredCats.get(1001);
        System.out.println("Porovnani kocek");
        System.out.println("Puvodni: " + writtenCat);
        System.out.println("Obnovena: " + restoredCat);
    }
}
