To compile:
-----------

1- excecute: mvn clean package

2 - copy the file the result file "target/NLP-1-jar-with-dependencies.jar" to another folder
3 - copy the file "resources/en-parser-chunking.bin" to the same folder created in the step 2
4 - copy the file "resources/en-sent.bin" to the same folder created in the step 2
5 - copy the file "resources/nlpTest.properties" to the same folder created in the step 2, you can edit the text, or change the provided bins for anothers.

Running the test:

6- Excecute the command: java -jar NLP-1-jar-with-dependencies.jar