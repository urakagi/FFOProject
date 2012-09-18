copy ..\dist\FantasyFesta.jar .
copy ..\dist\lib\* .\lib\
"C:\Program Files\7-Zip\7z.exe" a FantasyFestaCore.zip FantasyFesta.jar
"C:\Program Files\7-Zip\7z.exe" a FantasyFestaCore.zip lib\
copy FantasyFestaCore.zip C:\Users\Romulus\Documents\Dropbox\Public\FFO\
notepad C:\Users\Romulus\Documents\Dropbox\Public\FFO\version.txt
pause
