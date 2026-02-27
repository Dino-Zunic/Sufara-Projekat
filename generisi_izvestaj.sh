#!/bin/bash

IZVESTAJ="izvestaj.txt"
echo "SUFARA PROJEKAT - TEHNIČKI IZVEŠTAJ" > $IZVESTAJ
echo "Datum generisanja: $(date)" >> $IZVESTAJ
echo "--------------------------------------" >> $IZVESTAJ
echo "" >> $IZVESTAJ

echo "STRUKTURA DIREKTORIJUMA:" >> $IZVESTAJ
tree -I 'build|.git|node_modules|.idea|*.ttf|*.png|*.jpg' >> $IZVESTAJ
echo "" >> $IZVESTAJ
echo "--------------------------------------" >> $IZVESTAJ
echo "SADRŽAJ FAJLOVA:" >> $IZVESTAJ
echo "--------------------------------------" >> $IZVESTAJ

# Lista ekstenzija koje želimo da pročitamo
find . -type f \( -name "*.kt" -o -name "*.md" -o -name "*.gradle*" -o -name "*.xml" \) \
-not -path "*/build/*" \
-not -path "*/.git/*" \
-not -path "*/.idea/*" | while read -r fajl; do
    echo "" >> $IZVESTAJ
    echo ">>> PUTANJA: $fajl" >> $IZVESTAJ
    echo "--------------------------------------" >> $IZVESTAJ
    cat "$fajl" >> $IZVESTAJ
    echo "" >> $IZVESTAJ
    echo "<<< KRAJ FAJLA: $fajl" >> $IZVESTAJ
    echo "======================================" >> $IZVESTAJ
done

echo "Izveštaj je generisan u: $IZVESTAJ"
