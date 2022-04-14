package com.company;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashMap;

/**
 * Tälle luokalle voidaan luodaan yksittäisiä kuluolioita, suorittaa niillä laskutoimituksia ja
 * hakea niiden tietoja.
 */

public class Kulu implements Serializable {
    /**
     * vapaavalintainen merkkijonomuotoinen nimi kululle
     */
    String nimi;
    /**
     * vastaa yksittäisen tuotteen/tapahtuman summaa
     */
    double hinta;
    /**
     * ostopäivämäärä
     */
    LocalDate pvm;
    /**
     * kategoria, johon tuote/tapahtuma kuuluu. Valitaan listasta.
     */
    String kategoria;

    private static double kaikkienYhteissumma;

    private static HashMap<String, Double> hmKategoriat = new HashMap<>();

    /**
     * Alustaja, jolla luodaan yksittäinen kulu-olio. Kulu-olio voi vastata esimerkiksi yksittäistä ostettua
     * tuotetta tai kokonaista ostotapahtumaa.
     * @param nimi String, vapaavalintainen merkkijonomuotoinen nimi kululle
     * @param hinta double, vastaa tuotteen/tapahtuman summaa
     * @param pvm LocalDate, ostopäivämäärä
     * @param kategoria String-muotoinen kategoria, johon tuote/tapahtuma kuuluu
     */
    public Kulu(String nimi, double hinta, LocalDate pvm, String kategoria) {
        this.nimi = nimi;
        this.hinta = hinta;
        this.pvm = pvm;
        this.kategoria = kategoria;
    }

    /**
     * Uusia olioita luodessa pitää kirjaa kaikkien aiemmin tehtyjen olioiden hintojen yhteissumasta
     */

    public void yhteissummaaKaikki() {
        kaikkienYhteissumma += this.hinta;
    }

    /**
     * Piirakkakuvion luontia varten tehdään jokaisesta kategoriasta oma sektori, jonka suuruus vastaa siihen
     * kategoriaan kuuluvien kulu-olioiden yhteissummaa.
     */

    public void lisaaKategoriaSummaan() {
        if (hmKategoriat.containsKey(kategoria)) {
            double kategoriahinta = hmKategoriat.get(kategoria) + hinta;
            hmKategoriat.put(kategoria, kategoriahinta);
        }
        else {
            hmKategoriat.put(kategoria,hinta);
        }
    }

    /**
     * palauttaa yhteissumman, joka muodostuu jokaikisestä aiemmin luodun olion hinnasta.
     * @return private static double yhteissumma
     */

    public static double getKaikkienYhteissumma() {
        return kaikkienYhteissumma;
    }

    /**
     * palauttaa Hashmapin, joka koostuu sektorien nimistä ja niitä vastaavista sektorien suuruuksista.
     * @return private static hashmap, jonka key = sektorin nimi ja value = suuruus
     */

    public static HashMap<String, Double> getHmKategoriat() {
        return hmKategoriat;
    }

    /**
     * Antaa kyseisen olion päivämäärän.
     * @return LocalDate-muodossa olion ostotapahtuman päivämäärä (muotoa vvvv-kk-pp)
     */
    public LocalDate getPvm() {
        return pvm;
    }

    /**
     * Antaa kyseisen olion tiedot, jotka on luotu oliota alustettaessa.
     * @return String-merkkijono, joka sisältää kuluolion nimen, hinnan, ostopäivämäärän ja kategorian.
     */

    @Override
    public String toString() {
        return "Kulu{" +
                "nimi='" + nimi + '\'' +
                ", hinta=" + hinta +
                ", pvm=" + pvm +
                ", kategoria='" + kategoria + '\'' +
                '}';
    }
}
