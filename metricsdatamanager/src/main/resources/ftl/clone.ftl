<html>
<head>
  <title>McSheep - View of ${clone}</title>
  <div>
  <script language="javascript" type="text/javascript">
 var keyWords = new Array( "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while", "true", "false", "null" );
 var flowerBraces = new Array("}","{");
 var finalText = "";
 var keyWordStartTag = "<span style='font-weight:bold;color:#7B0052;'>"
  
var preStartTag = "<pre style='text-align: left; border: 1px dashed #008DEF; line-height: 18px; padding: 15px; font-size: 13px; font-family:'Courier New', Courier, monospace; overflow: auto;'>";
 
 var flowerBraceStartTag ="<span style='font-weight:bold;color:#D3171B'>"
 
 var stringStartTag = "<span style='color:#2A00FF'>";
 
 var commentStartTag = "<span style='color:#3F7F5F'>";
 
 var javaDocStartTag = "<span style='color:#3F5FBF'>";
 
 var annotationStartTag = "<span style='color:#646464'>"
   
 var append = true;
 var spanEndtag = "</span>"
 var preEndTag = "</pre>"
 
 function trim(str) {
        return str.replace(/^\s+|\s+$/g,"");
    }
    
function startsWith(subString, sourceString){
    if(sourceString.substr(0, subString.length) == subString){
      return true;
    }
    return false;
}

function processKeyWord(token){
 //alert("Processing Token :"+token );
 if(token == undefined) return;
 //check if it is present in the keyWord list
 if(keyWords.indexOf(token) != -1){
   enclosedText = keyWordStartTag + token + spanEndtag;
   finalText = finalText.substring(0,finalText.length-token.length);
   finalText = finalText + enclosedText;
   //alert("finalText :" + finalText );
  }
}

function processFlowerBraces(flowerBrace){
 formattedBrace = flowerBraceStartTag + flowerBrace + spanEndtag;
 
 finalText = finalText + formattedBrace;
 append = false;
}

function processDoubleQuotes(index,sourceText){

 var nextChar = "";
 var stringLiteral = '"';
 //alert("Found \" at index "+index);
 //read everything till the next "
 //be CAREFUL about the escape sequence
 while(nextChar!='"'){
  index++;
  nextChar = sourceText.charAt(index);
  //alert("Next Char = "+nextChar+" at "+index);
  if(nextChar == '\\'  ){
   switch(sourceText.charAt(index+1)){
    case '"':
      stringLiteral = stringLiteral + "\\\"";
      index++;
      break;
    case '\\':
      stringLiteral = stringLiteral + "\\\\";
      index++;
      break;
    default:
      stringLiteral = stringLiteral + nextChar;
      break;
    
   }//end of switch
   //escape sequenced \\ or \" found , dont end parse here
  }else if(nextChar == '<' || nextChar == '>'){
   stringLiteral = stringLiteral + htmlEscape(nextChar);
  }else{ 
   stringLiteral = stringLiteral + nextChar;
  }
 }//end of while
 
 //paint it blue
 paintedString = stringStartTag + stringLiteral + spanEndtag;
 finalText = finalText + paintedString;
 return index;

}

function processSingleQuotes(index,sourceText){
 var nextChar = "";
 var stringLiteral = "'";
 //alert("Found \" at index "+index);
 //read everything till the next 
 //be CAREFUL about the escape sequence
 while(nextChar!="'"){
  index++;
  nextChar = sourceText.charAt(index);
  //alert("Next Char = "+nextChar+" at "+index);
  if(nextChar == '\\'  ){
   switch(sourceText.charAt(index+1)){
    case "'":
      stringLiteral = stringLiteral + "\\\'";
      index++;
      break;
    case "\\":
      stringLiteral = stringLiteral + "\\\\";
      index++;
      break;
    default:
      stringLiteral = stringLiteral + nextChar;
      break;
    
   }//end of switch
   //escape sequenced \\ or \' found , dont end parse here
  }else if(nextChar == '<' || nextChar == '>'){
   stringLiteral = stringLiteral + htmlEscape(nextChar);
  }else{
   stringLiteral = stringLiteral + nextChar;
  }
 }//end of while
 
 //paint it blue
 paintedString = stringStartTag + stringLiteral + spanEndtag;
 finalText = finalText + paintedString;
 return index;
}

function processMultilineComment(index,sourceText){
  var nextChar = "";
  var multiLineComment = "/*";
  /*The current index points at /, we will increment it by 1
   because stringLiteral has already been filled with 2 chars
   Why increment by 1?
   Because we start the loop below by incrementing the index  
   */
  index++;
  
  //read everything until */ is found
  while(true){
   index++;
   nextChar = sourceText.charAt(index);
   if(nextChar == '*' && sourceText.charAt(index+1) =='/' )
    break;
   if(nextChar == '<' || nextChar == '>'){
    multiLineComment = multiLineComment + htmlEscape(nextChar);
   }else{
    multiLineComment = multiLineComment + nextChar;
   }
  }
  
  multiLineComment += "*/";
  var paintedMLComment =""
  if(startsWith("/** ",multiLineComment) || startsWith("/**\t",multiLineComment) || startsWith("/**\n",multiLineComment)){
   paintedMLComment = javaDocStartTag + multiLineComment + spanEndtag;
  }else{
   paintedMLComment = commentStartTag + multiLineComment + spanEndtag;
  }
  finalText = finalText + paintedMLComment;
  index++;//point it to the / char which ends the comment
  return index;
  
}

function processSingleLineComment(index,sourceText){
  var nextChar = "";
  var singleLineComment = "//";
  /*The current index points at /, we will increment it by 1
   because stringLiteral has already been filled with 2 chars
   Why increment by 1?
   Because we start the loop below by incrementing the index  
   */
  index++;
  
  //read everything until newline --&gt; \n is found
  while(true){
   index++;
   nextChar = sourceText.charAt(index);
   if(nextChar == '\n' )
    break;
   if(nextChar == '<' || nextChar == '>'){
    singleLineComment = singleLineComment + htmlEscape(nextChar);
   }else{
    singleLineComment = singleLineComment + nextChar;
   }
  }
  
  singleLineComment += "\n";
  var paintedComment = commentStartTag + singleLineComment + spanEndtag;
  finalText = finalText + paintedComment;
  return index;

}


function htmlEscape(currentChar){
 if(currentChar == '<')
  return "&lt;"
 else if(currentChar == '>')
  return "&gt;"
}
//TO BE DONE
function processAnnotations(){
  var nextChar = "";
  var annotation = "@";
  
}

function java2html(){
 finalText =""
 document.getElementById("html_code").value = "";
 document.getElementById("output").innerHTML =  "";

 var sourceText = document.getElementById("java_source").value;
 sourceText = trim(sourceText);
 var readToken ="";
 var index = 0
 for (index=0 ;index < sourceText.length ; index++){
 var currentChar = sourceText.charAt(index);
 //alert("Current Char = "+currentChar);
 append = true;
 
 if (currentChar == ';' || currentChar == '\t' || currentChar == ' ' || currentChar == '\n' || currentChar == '(' || currentChar == ')'){
  processKeyWord(readToken);
  finalText = finalText + currentChar;
  readToken = "";
  append = false; 
  }else if (currentChar =="+" || currentChar == '-' ||  currentChar == '*' || currentChar == '=' ){
    finalText = finalText + currentChar;
    readToken = "";
    append = false;
  }else if (currentChar == '}' || currentChar == '{' ){
    processKeyWord(readToken);
  processFlowerBraces(currentChar);
     readToken = "";
     append = false;
  } else if(currentChar =='"'){
    index = processDoubleQuotes(index,sourceText);
    readToken="";
  append = false;
  } else if(currentChar =="'"){
     index = processSingleQuotes(index,sourceText);
    readToken="";
  append = false;
  }else if(currentChar == '/' && sourceText.charAt(index+1) == '*'){
    //alert("processing MC comment");
    index = processMultilineComment(index,sourceText);
    readToken = "";
    append = false;
  }else if(currentChar == '/' && sourceText.charAt(index+1) == '/'){
    //alert("processing SingleLine comment");
    index = processSingleLineComment(index,sourceText);
    readToken = "";
    append = false;
  }else if(currentChar == '<' || currentChar == '>' ){
  currentChar = htmlEscape(currentChar);
  finalText = finalText + currentChar;    
    readToken = "";
    append = false;
  }/*else if(currentChar == '@'){
  processAnnotations(index,sourceText)    
    readToken = "";
    append = false;
  }*/
  else{
     readToken = readToken +  currentChar;
  }
  
  if(append){
   //alert("In append")
   finalText = finalText + currentChar;
  }

 }

  document.getElementById("html_code").value = preStartTag + finalText+ preEndTag;
  document.getElementById("output").innerHTML =  preStartTag + finalText + preEndTag
}
</script>
</div>
</head>
<body>
  <h1>McSheep - View of ${clone}</h1>
	<div id="java_source">
	${copy}
	</div>
	<div id="htmlcode">
	${paste}
	</div>
	<div id="output">
	${paste}
	</div>
<input onclick="java2html();" style="height: 50px;" type="button" value="Click Me to Convert !">
  	<p><a href="${repository.location}.html">Back</a>!
</body>
</html>