package com.mr.locationstrategiessimple;

class DataBase {

    Double id;
    Double latitude;
    Double longitude;
    Double accuracy;

    String time;
    String memo;
    String cdma;
    String gsm;
    String lte;

    public DataBase() {

    }

    public void DataBase(){

    }

    public DataBase(Double id, String time, Double latitude, Double longitude, Double accuracy, String memo, String cdma, String gsm, String lte) {
        this.id = id;
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.memo = memo;
        this.cdma = cdma;
        this.gsm = gsm;
        this.lte = lte;
    }

    public void setId(Double id) {
        this.id = id;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public void setCdma(String cdma) {
        this.cdma = cdma;
    }

    public void setGsm(String gsm) {
        this.gsm = gsm;
    }

    public void setLte(String lte) {
        this.lte = lte;
    }
}
