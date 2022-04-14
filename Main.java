package com.company;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Vastaa kaikista graafisen piirrännän elementeistä sekä tiedostonkäsittelystä koskien ohjelmaa.
 */

public class Main extends Application {

    //random alustuksia, jotta näkyvyys taataan kun kenttää käytetään useammassa metodissa.

    // Pohjapaneeli, jonka päälle kaikki muut paneelit laitetaan
    VBox pohjapaneeli = new VBox();

    // Piirakkakuvion paneeli ja itse piirakkadiagrammi
    Pane paneeliPiirakalle = new Pane();
    public PieChart piirakkakuvio = new PieChart();

    // Näitä käytetään apuna, kun haetaan jotain ajankohtaa vastaavia olioita. Paneelin yläreunassa.
    GridPane gpVuosiKuukausi = new GridPane();
    ComboBox cbVuosi = new ComboBox();
    ComboBox cbKuukausi = new ComboBox();
    TextField tfSumma = new TextField();
    Text syotaVuosi = new Text("Valitse vuosi, jonka valitsemaasi kuukautta tahdot tarkastella");

    // Alin paneeli, johon tulee tiedot kunkin kategorian nimestä, kokonaissummasta ja suhteellisesta osuudesta.
    GridPane osuudetKategorioittain = new GridPane();

    /**
     * Vastaa yleisestä ikkunan asettelusta. Ylimpänä comboboxit, joilla voi muuttaa tarkasteltavaa ajankohtaa,
     * seuraavana piirakkakuvio ja plus-nappi olioiden lisäämistä varten. Tämän jälkeen "yhteensä"-tiedot, jossa näkyy
     * näytöllä näkyvän piirakan sektoreiden summat yhteensä. Alimpana on tiedot kategorioittain.
     * @param alusikkuna sisältää kehyksen, joka puolestaan sisältää pohjapaneelin
     */

    public void start(Stage alusikkuna) {

        //  Seuraavan hboxin avulla valitaan, mikä kuukausi ja vuosi näytetään.

        gpVuosiKuukausi.setPadding(new Insets(30));

        Text annaVuosi = new Text("Anna tarkasteltava ajankohta: ");
        annaVuosi.setFont(Font.font(15));

        cbVuosi.getItems().add(0, null);
        int nykyinenVuosi = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = nykyinenVuosi; i > 1949; i--) {
            cbVuosi.getItems().add(i);
        }
        cbVuosi.setStyle("-fx-body-color: white");

        cbKuukausi.getItems().add(0, null);
        String[] kuukaudet = {"tammikuu", "helmikuu", "maaliskuu", "huhtikuu", "toukokuu",
                "kesäkuu", "heinäkuu", "elokuu", "syyskuu", "lokakuu", "marraskuu", "joulukuu"};
        cbKuukausi.getItems().addAll(kuukaudet);
        cbKuukausi.setStyle("-fx-body-color: white");

        Button btNayta = new Button("Näytä");
        String kaikkienSumma = String.valueOf(Kulu.getKaikkienYhteissumma());
        tfSumma.setText(kaikkienSumma);

        // Lisätään nodet ja asetellaan.
        gpVuosiKuukausi.addRow(0, annaVuosi, cbVuosi, cbKuukausi, btNayta);
        gpVuosiKuukausi.setHgap(5);
        gpVuosiKuukausi.setAlignment(Pos.CENTER);
        pohjapaneeli.getChildren().add(gpVuosiKuukausi);

        /* Asettelua luokan alussa määritellylle textfieldille.
        Jos yritetään syöttää pelkkää kuukautta ilman vuotta niin näytetään tämä teksti.
         */
        syotaVuosi.setFill(Color.RED);
        syotaVuosi.setFont(Font.font(13));

        // lambda-lauseke aiemmin luodulle napille. Painamalla näytetyt tiedot näytöllä päivittyvät.
        btNayta.setOnAction( e-> paivitaNaytto());

        //Paneeli piirakalle ja lisäysnappulalle
        Pane piirJaLisays = new Pane();

        //lisaysjutut
        Circle ympyraPlussalle = new Circle(30);
        ympyraPlussalle.setFill(Color.LIGHTPINK);

        Text plusmerkki = new Text( "+");
        plusmerkki.setFont(Font.font("Verdana", FontWeight.BOLD,30));
        plusmerkki.setFill(Color.GHOSTWHITE);

        StackPane lisaa = new StackPane();
        lisaa.getChildren().addAll(ympyraPlussalle, plusmerkki);
        lisaa.setTranslateY(150);
        lisaa.setTranslateX(575);

        /* lambda-lauseke oikeassa reunassa näkyvälle pinkkitaustaiselle +-merkille, jota painaessa
        avautuu aloitusikkunan päälle uusi ikkuna, jonka avulla luodaan olio.
         */
        lisaa.setOnMouseClicked( e -> piirrakulunaytto());

        /* Piirakkakuvio omaan paneeliinsa ja tämä paneeli samaan paneeliin aiemmin luodun napin kanssa.
        Tässä kohtaa piirakkakuvio on siis vielä tyhjä.
         */
        paneeliPiirakalle.getChildren().add(piirakkakuvio);
        piirJaLisays.getChildren().addAll(paneeliPiirakalle,lisaa);
        piirJaLisays.setTranslateY(5);

        // paneeli, joka sisältää piirakan ja lisäyspainikkeen laitetaan pohjapaneeliin.
        pohjapaneeli.getChildren().add(piirJaLisays);

        // Hbox, johon laitetaan "yhteensä:"-teksti ja sen perään textfield johon summa ilmenee.
        HBox hbYhteensa = new HBox();

        Text tYhteensa = new Text("Yhteensä: ");
        tYhteensa.setFont(Font.font(15));

        // textfield määritelty luokan alussa.
        tfSumma.setEditable(false);

        hbYhteensa.getChildren().addAll(tYhteensa, tfSumma);
        hbYhteensa.setAlignment(Pos.CENTER);
        hbYhteensa.setSpacing(10);
        hbYhteensa.setPadding(new Insets(20));

        // Laitetaan yhteensa-hboxin pohjapaneeliin
        pohjapaneeli.getChildren().add(hbYhteensa);

        // Asettelua luokan alussa alustetulle gridpanelle, johon tulee kategorioiden/sektoreiden tiedot.
        osuudetKategorioittain.setHgap(20);
        osuudetKategorioittain.setAlignment(Pos.CENTER);

        // Laitetaan gridpane pohjapaneeliin
        pohjapaneeli.getChildren().add(osuudetKategorioittain);

        Scene kehys = new Scene(pohjapaneeli, 700, 750);
        alusikkuna.setTitle("Kulut");
        alusikkuna.setScene(kehys);
        alusikkuna.show();
    }

    /**
     * Kun ohjelmaan halutaan lisätä uusi kulu, luodaan ikkuna kulu-olion luomista varten tämän metodin avulla.
     * Metodi saadaan pyörimään painamalla aiemmassa metodissa luotua pinkkitaustaista +-painiketta.
     */
    public void piirrakulunaytto() {
        // asettelua
        GridPane paneeliKulunlisaykselle = new GridPane();
        paneeliKulunlisaykselle.setVgap(10);
        paneeliKulunlisaykselle.setHgap(10);
        paneeliKulunlisaykselle.setPadding(new Insets(20));

        // labelit vasempaan reunaan
        paneeliKulunlisaykselle.add(new Label("Nimi: "), 0, 0);
        paneeliKulunlisaykselle.add(new Label("Hinta: "), 0, 1);
        paneeliKulunlisaykselle.add(new Label("Päivämäärä: "), 0, 2);
        paneeliKulunlisaykselle.add(new Label("Kategoria: "), 0, 3);

        // textfieldit nimen ja hinnan syöttämistä varten sekä datepicker päivämäärän valitsemista varten.
        TextField tfNimi = new TextField();
        TextField tfHinta = new TextField();
        DatePicker paivamaaraValitsin = new DatePicker();

        // Combobox, joka tulee aiempien nodejen perään. Sisältää ennalta määritetyt kategoriat.
        ComboBox kategoriaLista = new ComboBox();
        String[] kategoriat = {"asuminen", "liikkuminen", "ruoka", "terveys",
                "vaatteet", "kulttuuri ja vapaa-aika", "lainanlyhennys", "sijoitus", "muu"};
        kategoriaLista.getItems().addAll(kategoriat);

        // Buttoni, josta selviää myöhemmin, että se käynnistää olion luonnin.
        Button btLisaa = new Button("Lisää");

        // Varoitusteksti, jos olio yritetään lisätä ja joku kentistä on tyhjänä. Lisätään siis vasta btlisaan
        // painamisen jälkeen, mikäli tarve.
        Text varoitusteksti = new Text("Täytä kaikki kentät!");
        varoitusteksti.setFill(Color.RED);
        varoitusteksti.setFont(Font.font("Minecraftia", FontWeight.BOLD, 15));

        // Laitetaan kaikki valintahommelit yhteen sarakkeeseen.
        paneeliKulunlisaykselle.addColumn(1, tfNimi, tfHinta, paivamaaraValitsin, kategoriaLista, btLisaa);

        Scene skene2 = new Scene(paneeliKulunlisaykselle, 300, 230);

        Stage lisaysikkuna = new Stage();
        lisaysikkuna.setTitle("Kulunlisäys");
        lisaysikkuna.setScene(skene2);
        lisaysikkuna.show();


        // Kun painetaan nappia, luodaan olio sisällöistä ja suljetaan ikkuna.
        btLisaa.setOnAction(e -> {
            // Tarkastetaan että kaikkiin kenttiin on syötetty jtn
            if (tfNimi.getText().length()>0 && tfHinta.getText().length()>0 &&
                    paivamaaraValitsin.getValue()!=null && kategoriaLista.getValue()!=null) {

                // Muunnetaan saatuja tietoja parempaan muotoon.
                String nimi = tfNimi.getText();
                double hinta = Double.parseDouble(tfHinta.getText().replace(",", ".").
                        replace("€", ""));
                LocalDate pvm = paivamaaraValitsin.getValue();
                String kategoria = String.valueOf(kategoriaLista.getValue());

                /* Muunnetuista tiedoista luodaan olio ja metodien avulla lisätään olion hinta aiemmin luotujen olioiden
                yhteissummaan, sitä vastaavan kategorian yhteissummaan, kirjoitetaan se tiedostoon, luetaan
                se tiedostosta ja päivitetään alla oleva "pää"-ikkuna ja suljetaan olioluonti-ikkuna.
                 */
                Kulu kulu = new Kulu(nimi, hinta, pvm, kategoria);
                kulu.yhteissummaaKaikki();
                kulu.lisaaKategoriaSummaan();
                tiedostoonKirjoitus(kulu);
                tiedostostaLuku();
                paivitaNaytto();
                lisaysikkuna.close();
            }
            // jos tietoja ei oo täytetty (niinkuin pitää) niin näytetään aiemmin luotu varoitusteksti.
            else {
                paneeliKulunlisaykselle.add(varoitusteksti, 1, 5);
            }
        });
    }

    /**
     * Kun luodaan olio tai haetaan ikkuna yläreunassa olevien nodejen avulla jotain tiettyä ajankohtaa, kutsutaan
     * tötö metodia. Asettaa/päivittää oikean summan piirakan alla olevaan boksiin, kutsuu metodia, joka päivittää
     * piirakan sekä tarvittaessa esittää käyttäjälle vaatimuksen vuoden syöttämistä, jos yritetään tarkastella
     * tietoja syöttämällä pelkkä kuukausi.
     */

    public void paivitaNaytto () {
        // mikäli näytöllä näkyy virheviesti vuoden syöttämisestä, seuraava varmistaa että se poistuu
        gpVuosiKuukausi.getChildren().remove(syotaVuosi);
        /* jos molemmat comboboxit yläreunassa nulleja eli ilman valittua sisältöä, näytetään tiedot pohjautuen
         kaikkiin aiemmin luotuihin olioihin.
         */
        if (cbVuosi.getValue()==null & cbKuukausi.getValue()==null) {
            paivitaPiirakka(Kulu.getHmKategoriat());
            tfSumma.setText(String.format("%.2f",Kulu.getKaikkienYhteissumma()) + " €");
        }
        // näytetään virheviesit, mikä vuosi on null, mutta kuukausi on syötetty.
        else if (cbVuosi.getValue()==null & cbKuukausi.getValue()!=null) {
            gpVuosiKuukausi.add(syotaVuosi,0,1);
            GridPane.setColumnSpan(syotaVuosi, GridPane.REMAINING);
        }
        // Jos kuukautta ei ole erikseen valittu, mutta vuosi on, näytetään koko vuoden tiedot.
        else if (cbVuosi.getValue()!=null & cbKuukausi.getValue()==null) {

            // Koska tarkastellaan olioita vaan tietyltä vuodelta, luodaan niiden käsittelyyn oma hashmap
            HashMap<String, Double> valiaikaKategoria = new HashMap<>();
            // Vuosi, johon olioiden vuosia verrataan. Saadaan comboboxista
            String vertausVuosi = String.valueOf(cbVuosi.getValue());
            // Yhteissumma, johon lisätään kaikki kyseisen vuoden olioiden hinnat yhteen
            double yhteissumma = 0;

            // Käydään kaikkioliot läpi ja otetaan sieltä oikeat messiin
            for (Kulu olio : kaikkiOliot) {

                /*Kyseisen olion vuosi saadaan päivämäärän neljästä ensimmäisestä merkistä. Muunnetaan myös
                olion kategoria ja hinta sopivaan muotoon mahdollista jatkokäsittelyä varten
                 */
                String olioVuosi = (String.valueOf(olio.getPvm())).substring(0, 4);
                String olioKategoria = String.valueOf(olio.kategoria);
                double olioHinta = Double.parseDouble(String.valueOf(olio.hinta));

                // Katsotaan vastaako ensinnäkään olion vuosi comboboxiin syötettyä vuotta
                if (vertausVuosi.matches(olioVuosi)) {
                    /* Jos samaa kategoriaa vastaavia olioita on jo käyty aiemmin läpi ja laitettu hashmappiin
                    niin huomioidaan se kategorian arvon/suuruuden/hinnan lisäämisessä: haetaan kategorian
                    aiemmat tiedot ja lisätään olion hinta vain sinne perään
                     */
                    if (valiaikaKategoria.containsKey(olioKategoria)) {
                        valiaikaKategoria.put(olioKategoria, valiaikaKategoria.get(olioKategoria)+ olioHinta);
                    }
                    // Jos kategoriaa ei vielä ole niin laitetaan kategoriaa vastaavaks arvoks ekan olion hinta
                    else {
                        valiaikaKategoria.put(olioKategoria,olioHinta);
                    }
                    // Yhteissumma kaikkien tähän ajanjaksoon sopivien olioiden hinnoista
                    yhteissumma += olio.hinta;
                }
            }
            // Päivitetään piirakka ja laitetaan oikea summa näkyviin piirakan alle.
            paivitaPiirakka(valiaikaKategoria);
            tfSumma.setText(String.format("%.2f", yhteissumma) + " €");
        }
        /* Tähän haaraan mennään, jos on syötetty sekä vuosi että kuukausi. Toiminnallisuus lähestulkoon sama kuin
        edellisessä haarassa, eroavaisuudet kommentoitu.
         */
        else {
            HashMap<String, Double> valiaikaKategoria = new HashMap<>();
            String vertausVuosi = String.valueOf(cbVuosi.getValue());
            // Kuukausi, johon olion tietoja verrataan kuukauden lisäksi.
            int vertausKuukausi = muutaKuukausi(String.valueOf(cbKuukausi.getValue()));

            double yhteissumma = 0;

            for (Kulu olio : kaikkiOliot) {

                String olioVuosi = (String.valueOf(olio.getPvm())).substring(0, 4);
                // Olion kuukausi, johon comboboxiin syötettyä kuukautta verrataan.
                int olioKuukausi = Integer.parseInt(String.valueOf(olio.getPvm()).substring(5,7).replace("0", ""));

                String olioKategoria = String.valueOf(olio.kategoria);
                double olioHinta = Double.parseDouble(String.valueOf(olio.hinta));

                if ((vertausVuosi.matches(olioVuosi)) && vertausKuukausi == olioKuukausi) {
                    if (valiaikaKategoria.containsKey(olioKategoria)) {
                        valiaikaKategoria.put(olioKategoria, valiaikaKategoria.get(olioKategoria)+ olioHinta);
                    }
                    else {
                        valiaikaKategoria.put(olioKategoria,olioHinta);
                    }
                    yhteissumma += olio.hinta;
                }
            }
            paivitaPiirakka(valiaikaKategoria);
            tfSumma.setText(String.format("%.2f", yhteissumma) + " €");
        }
    }

    /**
     * Päivitään ikkunassa näkyvä piirakka
     * @param mappi hashmap, jonka key String-muotoinen =(sektorin nimi) ja value Double-muotoinen (sektorin suuruus)
     */
    public void paivitaPiirakka (Map<String, Double> mappi) {

        // Poistetaan vanha piirakkakuvio.
        paneeliPiirakalle.getChildren().remove(piirakkakuvio);

        // Luodaan Hashmapin avaimista ja arvoista omat listansa.
        String sektoriNimi [] = mappi.keySet().toArray(new String[0]);
        Double sektoriSuuruus [] = mappi.values().toArray(new Double[0]);

        // Luodaan data-lista, johon nää avaimet ja arvot sit laitetaan.
        PieChart.Data sektorit[] = new PieChart.Data[sektoriNimi.length];

        // Laitetaan data-listaan avaimet kuvastamaan sektorin nimeä ja niiden arvot kuvastamaan
        // sitä vastaavan sektorin suuruutta.
        for (int i=0; i<sektorit.length; i++) {
            sektorit[i] = new PieChart.Data(sektoriNimi[i], sektoriSuuruus[i]);
        }

        // Tehdään listasta Observablelist, jotta se sopii piechartille.
        ObservableList<PieChart.Data> olSektorit = FXCollections.observableArrayList(sektorit);

        // Asettelua
        piirakkakuvio.setLabelsVisible(false);
        piirakkakuvio.setTranslateX(175);
        piirakkakuvio.setMaxWidth(350);

        // Asetetaan uusi data piirakkakuvioon.
        piirakkakuvio.setData(olSektorit);

        // Lisätään piirakka takas paneeliin.
        paneeliPiirakalle.getChildren().add(piirakkakuvio);

        // Päivitetään vielä alla olevan metodikutsun avulla piirakan alla olevat kategoriatiedot.
        naytaKategorioittain((HashMap<String, Double>) mappi);
    }

    /**
     * Päivitetään kategoriatiedot (nimi, suuruus, suhteellinen suuruus) ikkunaan piirakan alle.
     * @param hashmap hashmap, jonka key String-muotoinen =(sektorin nimi) ja value Double-muotoinen (sektorin suuruus)
     */
    public void naytaKategorioittain (HashMap<String, Double> hashmap) {

        int riviNumero = 0;
        double kaikkiKategoriatYhteensa = 0;

        // Kun päivitetään kategoriapaneelia, poistetaan ensin vanhat nodet.
        for (int a = 0; a<osuudetKategorioittain.getRowCount(); a++) {
            int b = a;
            osuudetKategorioittain.getChildren().removeIf(node -> osuudetKategorioittain.getRowIndex(node) == b);
        }

        /* Haetaan kategorian olioiden yhteissumma, jotta saadaan luku, jonka
        avulla saadaan suhteelliset suuruudet myöhemmin.
         */
        for (Double yksilonHinta : hashmap.values()) {
            kaikkiKategoriatYhteensa += yksilonHinta;
        }

        // Käydään tän hashmapin kaikki oliot läpi.
        for (Map.Entry<String, Double> yksilo : hashmap.entrySet()) {
            // Kategorian/sektorin nimi
            Text kategoriaNimi = new Text(yksilo.getKey());

            // Kategorian suuruus ja sen muotoilua
            String strKategoriasuuruus = String.format("%.2f",yksilo.getValue()).replace(".", ",") + " €";
            Text kategoriaSuuruus = new Text(strKategoriasuuruus);

            // Kategorian summan suhteellinen osuus annetun hashmapin kaikkien kategorioiden yhteissummasta.
            int intSuhteellinenOsuus = (int) Math.round((yksilo.getValue()/kaikkiKategoriatYhteensa)*100);
            String strSuhteellinenOsuus = intSuhteellinenOsuus + " %";
            Text tSuhteellinenOsuus = new Text(strSuhteellinenOsuus);

            // asettelua
            kategoriaNimi.setFont(Font.font(14));
            kategoriaSuuruus.setFont(Font.font(14));
            tSuhteellinenOsuus.setFont(Font.font(14));

            // laitetaan paneeliin rivi aiemmista tiedoista.
            osuudetKategorioittain.addRow(riviNumero, kategoriaNimi, kategoriaSuuruus, tSuhteellinenOsuus);

            // kasvatetaan rivinuemroa, jotta seuraavan kategorian tiedot tulee seuraavalle riville
            riviNumero++;
        }
    }

    /**
     * Muuntaa annetun kuukaudennimen sitä vastaavaksi numeroksi.
     * @param kuukausinimi String-muodossa, suomenkielellä, pienillä kirjaimilla kuukauden nimi
     * @return int numero (väliltä 1-12), joka vastaa parametrina annettua kuukautta
     */
    public int muutaKuukausi(String kuukausinimi) {
        int kuukausinro = 0;
        switch (kuukausinimi) {
            case "tammikuu":
                kuukausinro = 1;
                break;
            case "helmikuu":
                kuukausinro = 2;
                break;
            case "maaliskuu":
                kuukausinro = 3;
                break;
            case "huhtikuu":
                kuukausinro = 4;
                break;
            case "toukokuu":
                kuukausinro = 5;
                break;
            case "kesäkuu":
                kuukausinro = 6;
                break;
            case "heinäkuu":
                kuukausinro = 7;
                break;
            case "elokuu":
                kuukausinro = 8;
                break;
            case "syyskuu":
                kuukausinro = 9;
                break;
            case "lokakuu":
                kuukausinro = 10;
                break;
            case "marraskuu":
                kuukausinro = 11;
                break;
            case "joulukuu":
                kuukausinro = 12;
                break;
        }
        return kuukausinro;
    }

    /**
     * Tiedoston nimi jota käytetään seuraavissa metodeissa
     */
    String tNimi = "oliot.dat";

    /**
     * Kirjoitetaan olio tiedostoon
     * @param olio kulu-olio, jonka kentät nimi, hinta, ostopäivämäärä ja kategoria
     */
    public void tiedostoonKirjoitus (Kulu olio) {
        ObjectOutputStream kirjoitaTiedosto = null;

        try {
            kirjoitaTiedosto = new ObjectOutputStream(new FileOutputStream(tNimi));
            kirjoitaTiedosto.writeObject(olio);
            kirjoitaTiedosto.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Kun luetaan tiedostosta olioita, käytetään seuraavaa listaa
    ArrayList<Kulu> kaikkiOliot = new ArrayList<>();

    /**
     * Luetaan olio tiedostosta arraylistin perään.
     */
    public void tiedostostaLuku () {
        ObjectInputStream lueTiedosto = null;
        try {
            lueTiedosto = new ObjectInputStream(new FileInputStream(tNimi));
            kaikkiOliot.add((Kulu) lueTiedosto.readObject());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * käynnistetään koko helahoito, tällä saadaan ikkuna näkyviin.
     * @param args parametri
     */
    public static void main(String[] args) {
        Application.launch(args);
    }
}
