package source;

import source.dataStructure.MyBuffer;
import source.dataStructure.MyReader;
import source.entity.Cat;
import source.generators.CatGenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        MyReader<Cat> myReader = new MyReader<>(1000, 4 + 20 + 8, true);

        File catFile = new File("cats");
        if(catFile.exists()){
            catFile.delete();
        }
        MyBuffer myBuffer = new MyBuffer(1000);
        List<Cat> catList = new ArrayList<>();
        for(int i = 0; i < 1000000; i++){
            Cat cat = CatGenerator.generateCat();
            catList.add(cat);
        }

        //Cat writtenCat = catList.get(100000);

        try {
            myBuffer.writeCatsToFile(catList, catFile);
        } catch (Exception e){
            throw new RuntimeException(e);
        }

        List<Cat> restoredCats = new ArrayList<>();
        try {
            restoredCats = myReader.readEntitiesFromFile(catFile);
        } catch (Exception e){
            throw new RuntimeException(e);
        }

        //Cat restoredCat = restoredCats.get(100000);
        System.out.println("Porovnani kocek");
        if(restoredCats.size() != catList.size()){
            throw new RuntimeException("Velikost zapsanych kocek a obnovenych se nerovna");
        }
        for(int i = 0; i < restoredCats.size(); i++){
            if(!restoredCats.get(i).equals(catList.get(i))){
                throw new RuntimeException("Kocky se nerovnaji");
            }
        }
        System.out.println("Program uspesne dobehl");
    }
}
