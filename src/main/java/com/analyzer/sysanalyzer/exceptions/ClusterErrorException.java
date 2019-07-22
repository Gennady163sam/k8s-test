package com.analyzer.sysanalyzer.exceptions;

public class ClusterErrorException extends Exception {

    public ClusterErrorException() {
    }

    public ClusterErrorException(String s) {
        super(s);
    }

    public ClusterErrorException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public ClusterErrorException(Throwable throwable) {
        super(throwable);
    }

    public ClusterErrorException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
