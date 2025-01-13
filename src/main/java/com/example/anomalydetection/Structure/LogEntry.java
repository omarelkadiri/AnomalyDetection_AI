package com.example.anomalydetection.Structure;

public class LogEntry {
    private String source_ip;
    private String dest_ip;
    private int source_port;
    private int dest_port;
    private long bytes;
    private String direction;

    public LogEntry() {
    }

    public LogEntry(String source_ip, String dest_ip, int source_port, int dest_port, long bytes, String direction) {
        this.source_ip = source_ip;
        this.dest_ip = dest_ip;
        this.source_port = source_port;
        this.dest_port = dest_port;
        this.bytes = bytes;
        this.direction = direction;
    }

    public String getSource_ip() {
        return source_ip;
    }

    public void setSource_ip(String source_ip) {
        this.source_ip = source_ip;
    }

    public String getDest_ip() {
        return dest_ip;
    }

    public void setDest_ip(String dest_ip) {
        this.dest_ip = dest_ip;
    }

    public int getSource_port() {
        return source_port;
    }

    public void setSource_port(int source_port) {
        this.source_port = source_port;
    }

    public int getDest_port() {
        return dest_port;
    }

    public void setDest_port(int dest_port) {
        this.dest_port = dest_port;
    }

    public long getBytes() {
        return bytes;
    }

    public void setBytes(long bytes) {
        this.bytes = bytes;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    @Override
    public String toString() {
        return "LogEntry{" +
                "source_ip='" + source_ip + '\'' +
                ", dest_ip='" + dest_ip + '\'' +
                ", source_port=" + source_port +
                ", dest_port=" + dest_port +
                ", bytes=" + bytes +
                ", direction='" + direction + '\'' +
                '}';
    }
}