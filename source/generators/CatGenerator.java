package source.generators;

import source.entity.Cat;

import java.util.Random;

public class CatGenerator {
    private final static Random random = new Random();
    private final static char[] alphabet = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

    public static Cat generateCat(){
        StringBuffer catNameBuilder = new StringBuffer();
        int id = random.nextInt();
        double weight = random.nextDouble() * 10 + 1;
        int catNameLength = random.nextInt(10) + 1;
        for(int i = 0; i < 10; i++){
            if(i <= catNameLength){
                int letterIndex = random.nextInt(alphabet.length);
                catNameBuilder.append(alphabet[letterIndex]);
            } else {
                catNameBuilder.append(' ');
            }
        }
        Cat cat = new Cat(id, catNameBuilder.toString(), weight);
        return cat;
    }
}
