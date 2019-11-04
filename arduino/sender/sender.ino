#include "pitches.h"

//define the string to spell using their respective "binary" arrrays in pitches.h file
int* spell_name[] = {
  NOTE_A, NOTE_s, NOTE_i, NOTE_m, NOTE_SPACE, NOTE_F, NOTE_a, NOTE_u, NOTE_z, NOTE_i, NOTE_NEWLINE //"Asim\wFauzi\n"
};

//start the note on 19kHz frequency (all freqs later to be multiplied by 1000
int start_note = 19;
int current_note = start_note;

//do nothing
void setup() {
  //nothing here
}

//loop through this thing until done...
void loop() {

  //loop through spell_name (has all my name's letters) then loop through each letter's binary digits and play the corresponding note (or frequency)
  for(int i = 0; i < sizeof(spell_name); i++){
    for(int j = 0; j < sizeof(spell_name[i]); j++){
      tone(8, nextNote(current_note, spell_name[i][j]), 25);
    }
  }
  
}

/*
  19kHz -> 20kHz = 0 
  21kHz -> 20kHz = 0
  20kHz -> 21kHz = 0
  19kHz -> 21kHz = 1
  20kHz -> 19kHz = 1
  21kHz -> 19kHz = 1

  plays the next note based on the current note and what binary digit is needed
*/
int nextNote(int current, int want){
  switch(want) {
    case 0:
       switch(current){
        case 19:
        current_note = 20;
        return 20;
        break;
        case 20:
        current_note = 21;
        return 21;
        break;
        case 21:
        current_note = 20;
        return 20;
        break;
      }
     break;
    case 1:
       switch(current){
        case 19:
        current_note = 21;
        return 21;
        break;
        case 20:
        current_note = 19;
        return 19;
        break;
        case 21:
        current_note = 19;
        return 19;
        break;
      }
     break;
  }
}
