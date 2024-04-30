package source.dataStructure;

import source.entity.Cat;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyBuffer {
    private byte[] bufferData;
    private int bufferSize;
    private int numberOfWrittenEntities;
    private int entitySize;
    public int numbersUsed = 0;

    public MyBuffer(int bufferEntityCapacity){
        //int (4), string (10 chars = 20B), double(8)
        this.entitySize = 4 + 20 + 8;
        bufferSize = entitySize * bufferEntityCapacity;
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

    public void setNumberOfWrittenEntities(int numberOfWrittenEntities) {
        this.numberOfWrittenEntities = numberOfWrittenEntities;
    }

    public boolean isFull(){
        return numberOfWrittenEntities * entitySize >= bufferSize;
    }

    public boolean isEmpty(){
        return numberOfWrittenEntities <= 0;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public int getEntitySize() {
        return entitySize;
    }

    public int getNumberOfWrittenEntities() {
        return numberOfWrittenEntities;
    }

    public byte[] getBufferData() {
        return bufferData;
    }
}
