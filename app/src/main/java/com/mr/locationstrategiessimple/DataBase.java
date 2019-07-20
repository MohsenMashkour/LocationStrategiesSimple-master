package com.mr.locationstrategiessimple;

class DataBase {

    public Double id;
    public Double latitude;
    public Double longitude;
    public Double accuracy;

    public String time;
    public String memo;

    public int cdmaDbm;
   // public int gsm;
    //public int lte;

    public int lteSignalStrength;
    public int lteCqi;
    public int lteRssnr;
    public int lteRsrp;
    public int gsmBitErrorRate;
    public int cdmaEcIo;
    public int gsmSignalStrength;


    public DataBase() {

    }

    public void DataBase(){

    }



    public DataBase(Double id, String time, Double latitude, Double longitude, Double accuracy, String memo, int cdmaDbm, int lteSignalStrength,
                    int gsmSignalStrength, int lteCqi, int lteRssnr, int lteRsrp, int gsmBitErrorRate, int cdmaEcIo) {
        this.id = id;
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.memo = memo;
        this.cdmaDbm = cdmaDbm;
       // this.gsm = gsm;
        //this.lte = lte;

        this.lteSignalStrength = lteSignalStrength;
        this.gsmSignalStrength = gsmSignalStrength;
        this.lteCqi = lteCqi;
        this.lteRssnr = lteRssnr;
        this.lteRsrp = lteRsrp;
        this.gsmBitErrorRate = gsmBitErrorRate;
        this.cdmaEcIo = cdmaEcIo;
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

    public void setCdmaDbm(int cdmaDbm) {
        this.cdmaDbm = cdmaDbm;
    }

   // public void setGsm(int gsm) {        this.gsm = gsm;    }

   // public void setLte(int lte) { this.lte = lte;    }

    public void setLteSignalStrength(int lteSignalStrength) { this.lteSignalStrength = lteSignalStrength; }

    public void setLteCqi(int lteCqi) {
        this.lteCqi = lteCqi;
    }

    public void setLteRssnr(int lteRssnr) {
        this.lteRssnr = lteRssnr;
    }

    public void setLteRsrp(int lteRsrp) {
        this.lteRsrp = lteRsrp;
    }

    public void setGsmBitErrorRate(int gsmBitErrorRate) {
        this.gsmBitErrorRate = gsmBitErrorRate;
    }

    public void setCdmaEcIo(int cdmaEcIo) {
        this.cdmaEcIo = cdmaEcIo;
    }

    public void setGsmSignalStrength(int gsmSignalStrength) { this.gsmSignalStrength = gsmSignalStrength;  }
}
