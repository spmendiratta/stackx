_stackx
_Add apache http client jars to CLASSPATH (jars in ./jars)
(Site: https://hc.apache.org/httpcomponents-client-4.5.x/download.html)

i_javac *.java
_java process_sx

<<
usage: java process_sx -s <site> -t <tag(s)> -m <months> -q <query>
       <site>: (st)ackexchange
               (se)rverfault
               (su)peruser
     <tag(s)>: "tag1;tag2.."
     <months>: go back m months
      <query>: (q)uestions
               (a)nswers
               (c)omments
               (p)osts
   Start Date: today - 30*months days 
     End Date: today

ex: java process_sx -s st -t "oracle;linux" -m 12 -q q
get all questions from site stackexchange tagged oracle&linux for last year

output files in this instance:
stack_excahnge_oracle_linux.json (back from stackexchange)
stack_excahnge_oracle_linux_Final.json (file with answers where a question is answered and 
accepted_question_id is not null)
>>
