package Model;


public interface Functions {

    /**
     * Ovaj interface sadrzi metode koje se koriste za upravljanje muzickim plejerom.
     * Metode koje se nalaze u ovom interfejsu su playSong, resumeSong, stopSong, pauseSong, nextSong i prevSong.
     * Ove metode se koriste za pustanje pesme, nastavljanje pesme, zaustavljanje pesme, pauziranje pesme,
     * prelazak na sledecu pesmu i prelazak na prethodnu pesmu.
     * Implementacija ovog interfejsa se nalazi u klasi MusicPlayer.
     */

    void playSong();
    void resumeSong();
    void stopSong();
    void pauseSong();
    void nextSong();
    void prevSong();

}