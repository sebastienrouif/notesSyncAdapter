adb -d shell 'run-as org.rouif.notes cat /data/data/org.rouif.notes/databases/notes.db > /sdcard/notes.db' && adb pull /sdcard/notes.db