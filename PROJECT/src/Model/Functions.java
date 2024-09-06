package Model;


/**
 * Ovaj interface sluzi za definiranje metoda koji se koriste u MusicPlayer klasi.
 * Pomocu njega se definiraju metode koje se koriste za pustanje, zaustavljanje, pauziranje, prethodnu i sljedecu pjesmu.
 */
public interface Functions {
    void playSong();
    void resumeSong();
    void stopSong();
    void pauseSong();
    void nextSong();
    void prevSong();

}