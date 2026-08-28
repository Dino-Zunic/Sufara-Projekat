# Kratak pregled koda

## Kako je aplikacija podeljena

`app` povezuje celu aplikaciju. `MainActivity` pravi Room bazu i repozitorijum, postavlja temu, a `SufaraNavGraph` povezuje glavni meni, mapu, lekcije, pisanje, obnavljanje i podešavanja.

`core:designsystem` sadrži boje, fontove, temu i zajedničke Compose elemente. Tu su geometrijska pozadina, zlatna linija na dugmadima i animacija zvezdica.

`feature:lesson` sadrži najveći deo logike:

- `data/local` definiše Room tabele i DAO za napredak lekcija, pisanja i kvizova.
- `data/repository` učitava sadržaj iz asseta, uparuje audio, parsira kvizove i čuva raspored obnavljanja.
- `domain/model` sadrži modele lekcije, koraka i meharidža.
- `domain/util` sadrži IPA transkripciju, razdvajanje arapskih grafema, proveru rukopisa i male čiste algoritme.
- `presentation/grid` prikazuje zajedničku 2D mapu čitanja i pisanja.
- `presentation/viewer` prikazuje teoriju, pitanja i kartice sa primerima i zvukom.
- `presentation/writing` pravi kanonski vodič iz arapskog fonta, prima putanje prsta i ocenjuje sličnost.
- `presentation/anki` vodi sesiju obnavljanja i vraća pogrešna pitanja u isti red.
- `presentation/settings` čuva izgled i eksperimentalne opcije u `SharedPreferences`.

Prazni moduli `core:common`, `core:database`, `core:network`, `feature:map` i `feature:spectrogram` trenutno nemaju aktivnu logiku.

## Tok podataka

Fascikle lekcija se sortiraju po brojčanom prefiksu iz imena. Taj prefiks ostaje stabilni ID, dok aplikacija posebno dodeljuje uzastopni redni broj za mapu. `симболи.md` se uparuje sa tako sortiranim fasciklama.

Svaki neprazan red u `примери.md` postaje kartica i vežba pisanja. Snimci iz `lekcije/audio/<ID>` sortiraju se leksikografski i redom se vezuju za te kartice. Ako snimak nedostaje ili postoji višak, aplikacija beleži problem i nastavlja bez rušenja.

Napredak je u Room bazi. Tačan odgovor dobija naredni termin po rasporedu, dok pogrešan ostaje odmah dostupan i vraća se u tekuću sesiju.

## Važni algoritmi

`ArabicIpaTranscriber` čita osnovno slovo zajedno sa njegovim znacima. Obrađuje duge vokale, šeddu, tenvin, sukun, teške glasove, rā, skriveni elif, reč Allah, određeni član i hamzetul-vasl.

`WritingReferenceFactory` prvo renderuje arapski tekst sa velikom rezervom, nalazi stvarne vidljive granice i uklapa ih u kanonski raster sa paddingom. `WritingMatcher` zatim poredi centralne linije oblika sa putanjama prsta. Sitno podrhtavanje i mali paralelni pomak su dozvoljeni, ali nedostajući delovi i dodatno šaranje obaraju rezultat.

## Provera izmena

Koristi JDK iz Android Studio instalacije ili drugi JDK 17 i pokreni:

```powershell
./gradlew.bat test lint assembleDebug
```

Za brzu proveru lesson modula:

```powershell
./gradlew.bat :feature:lesson:testDebugUnitTest
```

Pre predaje treba pregledati ceo Git diff i proveriti da sadržaj lekcija nije slučajno prepisan.
