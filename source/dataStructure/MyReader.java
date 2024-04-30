package source.dataStructure;

import source.entity.Cat;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MyReader<T> {
    private int bufferCapacity;
    private int entitySize;
    private int bufferSize;
    private boolean doubleBuffered;
    private MyBuffer buffer1;
    private MyBuffer buffer2;
    private int currentOffset = 0;
    private final Lock lock = new ReentrantLock();

    List<Cat> entities = new ArrayList<>();

    public MyReader(int bufferCapacity, int entitySize, boolean doubleBuffered) {
        this.bufferCapacity = bufferCapacity;
        this.doubleBuffered = doubleBuffered;
        //TODO genericky?
        this.entitySize = entitySize;
        this.bufferSize = entitySize * bufferCapacity;
        this.buffer1 = new MyBuffer(bufferCapacity);
        this.buffer2 = new MyBuffer(bufferCapacity);
    }

    public List<Cat> getEntities(){
        return entities;
    }

    public List<Cat> readEntitiesFromFile(File file) throws IOException, InterruptedException {
        List<Cat> cats = new ArrayList<>();
        if(doubleBuffered) {
            Thread buffer1Thread = new Thread(() -> {
                try {
                    innerReadEntitiesFromFile(file, cats, buffer1);
                } catch (IOException e) {
                    throw new RuntimeException("Chyba v buffer1: " + e.getMessage());
                }
            });
            Thread buffer2Thread = new Thread(() -> {
                try {
                    innerReadEntitiesFromFile(file, cats, buffer2);
                } catch (IOException e) {
                    throw new RuntimeException("Chyba v buffer2: " + e.getMessage());
                }
            });
            buffer1Thread.start();
            buffer2Thread.start();
            buffer1Thread.join();
            buffer2Thread.join();

            System.out.println("Buffer1: " + buffer1.numbersUsed);
            System.out.println("Buffer2: " + buffer2.numbersUsed);

            return cats;
        } else {
            return innerReadEntitiesFromFile(file, cats, buffer1);
        }
    }

    private void addToEntityOutput(List<Cat> cats, List<Cat> catsFromFile) {
        cats.addAll(catsFromFile);
    }

    //TODO do nejakeho readeru, bude koordinovat buffery
    private List<Cat> innerReadEntitiesFromFile(File file, List<Cat> returnedEntities, MyBuffer buffer) throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");

        if(doubleBuffered){
            //boolean endOfFileReached = fillBufferFromFile(randomAccessFile, currentOffset, buffer) != -1;
            while(fillBufferFromFile(randomAccessFile, buffer) != -1){
                List<Cat> entitiesInBuffer = getCatsAndIncrementBuffer(returnedEntities, buffer);
                lock.unlock();
                for (Cat cat : entitiesInBuffer) {
                    System.out.println(cat);
                }
            }
        } else {
            while (fillBufferFromFile(randomAccessFile, buffer) != -1) {
                List<Cat> entitiesInBuffer = getStoredEntities(buffer);
                addToEntityOutput(returnedEntities, entitiesInBuffer);
                currentOffset++;
                buffer.numbersUsed++;
                System.out.println("Buffer: " + buffer.numbersUsed);
                for (Cat cat : entitiesInBuffer) {
                    System.out.println(cat);
                }
            }
        }

        if(doubleBuffered) {
            try {
                lock.unlock();
            } catch (IllegalMonitorStateException e) {
                System.out.println("Pokus o uvolneni nezamceneho zamku");
            }
        }
        return returnedEntities;
    }

    private List<Cat> getCatsAndIncrementBuffer(List<Cat> returnedEntities, MyBuffer buffer) throws IOException {
        List<Cat> entitiesInBuffer = getStoredEntities(buffer);
        addToEntityOutput(returnedEntities, entitiesInBuffer);
        buffer.numbersUsed++;
        System.out.println("Buffer: " + buffer.numbersUsed);
        currentOffset++;
        return entitiesInBuffer;
    }

    public List<Cat> getStoredEntities(MyBuffer buffer) throws IOException {
        List<Cat> entities = new ArrayList<>();
        for(int i = 0; i < buffer.getNumberOfWrittenEntities() * entitySize; i+=entitySize){
            byte[] catBytes = new byte[entitySize];
            for(int j = 0; j < catBytes.length; j++){
                catBytes[j] = buffer.getBufferData()[i+j];
            }
            entities.add(convertFromBytes(catBytes));
        }
        return entities;
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

    private int fillBufferFromFile(RandomAccessFile file, MyBuffer buffer) throws IOException {
        if(doubleBuffered) {
            lock.lock();
        }
        file.seek((long) currentOffset * bufferSize);
        long nextOffsetInFile = (file.getFilePointer() + bufferSize);
        long numberOfBytesToRead = bufferSize;

        if(nextOffsetInFile > file.length()){
            numberOfBytesToRead = file.length() - file.getFilePointer();
            if(numberOfBytesToRead < 0) return -1;
        }

        int numberOFBytesRead = file.read(buffer.getBufferData(), 0, (int)numberOfBytesToRead);
        if(numberOFBytesRead != -1) {
            buffer.setNumberOfWrittenEntities(numberOFBytesRead / entitySize);
        } else {
            buffer.setNumberOfWrittenEntities(0);
        }
        return numberOFBytesRead;
    }
}
