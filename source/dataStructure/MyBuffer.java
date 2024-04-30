package source.dataStructure;

import source.entity.Cat;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyBuffer<T> {
    private byte[] bufferData;
    private int bufferSize;
    private int numberOfWrittenEntities;
    private int entitySize;

    public MyBuffer(int maxNumberOfEntitiesPerOperation){
        //int (4), string (10 chars = 20B), double(8)
        this.entitySize = 4 + 20 + 8;
        bufferSize = entitySize * maxNumberOfEntitiesPerOperation;
        this.bufferData = new byte[bufferSize];
    }

    public void writeCatToBuffer(Cat cat) throws IOException {
        byte[] catBytes = convertToBytes(cat);
        for(int i = 0; i < catBytes.length; i++){
            bufferData[i + (numberOfWrittenEntities * entitySize)] = catBytes[i];
        }
        numberOfWrittenEntities++;
    }

    //TODO do nejakeho readeru, bude koordinovat buffery
    public void writeCatsToFile(List<Cat> cats, File file) throws IOException {
        for (Cat cat : cats) {
            writeCatToBuffer(cat);
            if(isFull()){
                writeAllBytesToFile(file);
                emptyBuffer();
            }
        }
        if(!isEmpty()){
            writeAllBytesToFile(file);
        }
    }

    //TODO do nejakeho readeru, bude koordinovat buffery
    public List<Cat> readCatsFromFile(File file) throws IOException, ClassNotFoundException {
        List<Cat> returnedCats = new ArrayList<>();
        int currentOffset = 0;
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
        while(fillBufferFromFile(randomAccessFile, currentOffset) != -1){
            returnedCats.addAll(getStoredEntities());
            currentOffset++;
        }
        return returnedCats;
    }

    private void emptyBuffer() {
        bufferData = new byte[bufferSize];
        numberOfWrittenEntities = 0;
    }

    private void writeAllBytesToFile(File file) throws IOException {
        if(!file.exists()){
            file.createNewFile();
        }
        Files.write(file.toPath(), Arrays.copyOfRange(bufferData, 0, entitySize * numberOfWrittenEntities), StandardOpenOption.APPEND);
    }

    private int fillBufferFromFile(RandomAccessFile file, int readFromPosition) throws IOException {
        //Nemelo by vyvolat vyjimku - viz kontrakt
        file.seek((long) readFromPosition * bufferSize);
        long nextOffsetInFile = (file.getFilePointer() + bufferSize);
        long numberOfBytesToRead = bufferSize;

        if(nextOffsetInFile > file.length()){
            numberOfBytesToRead = file.length() - file.getFilePointer();
            if(numberOfBytesToRead < 0) return -1;
        }

        int numberOFBytesRead = file.read(bufferData, 0, (int)numberOfBytesToRead);
        if(numberOFBytesRead != -1) {
            numberOfWrittenEntities = numberOFBytesRead / entitySize;
        } else {
            numberOfWrittenEntities = 0;
        }
        return numberOFBytesRead;
    }

    private byte[] convertToBytes(Cat cat) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(bos))
        {
            dos.writeInt(cat.getId());
            dos.writeChars(cat.getNazev());
            dos.writeDouble(cat.getVaha());
            return bos.toByteArray();
        }
    }

    private Cat convertFromBytes(byte[] bytes) throws IOException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             DataInputStream dis = new DataInputStream(bis))
        {
            int id = dis.readInt();
            char[] nazevChars = new char[10];
            for(int i = 0; i < nazevChars.length; i++){
                nazevChars[i] = dis.readChar();
            }
            String nazev = String.valueOf(nazevChars);
            double vaha = dis.readDouble();
            return new Cat(id, nazev, vaha);
        }
    }

    public List<Cat> getStoredEntities() throws IOException, ClassNotFoundException {
        List<Cat> cats = new ArrayList<>();
        for(int i = 0; i < numberOfWrittenEntities * entitySize; i+=entitySize){
            byte[] catBytes = new byte[entitySize];
            for(int j = 0; j < catBytes.length; j++){
                catBytes[j] = bufferData[i+j];
            }
            cats.add(convertFromBytes(catBytes));
        }
        return cats;
    }

    public boolean isFull(){
        return numberOfWrittenEntities * entitySize >= bufferSize;
    }

    public boolean isEmpty(){
        return numberOfWrittenEntities <= 0;
    }
}
