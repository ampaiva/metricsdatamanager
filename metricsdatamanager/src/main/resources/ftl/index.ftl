<html>
<head>
 <title>McSheep - Clones Visualization</title>
  <link rel="stylesheet" type="text/css" href="stylesheets/clones.css">
 </head>
<body>
	<h1>McSheep - Clones Visualization</h1>
 	<h2>This table contains the number of code clones detected by McSheep and PMD for each system. Each number links to respective code clones.</h2>
  	<br><#list repositories>
		<div style="overflow-x:auto;">
          <#assign pna=3>
		  <table id="tableid2">
		   <tr>
		       <th rowspan="3">#
		       <th rowspan="3">System
			   <#list tools as tool>
		          <th title="${tool} results" colspan="${resultfolders?size*pna}">${tool}
			   </#list>
		   </tr>
		   <tr>
			   <#list tools as tool>
			       <#list resultfolders as resultfolder>
						<#if tool == "McSheep">
							<#assign name=resultfolder.name>
						<#else>
							<#assign name="100">
						</#if>                	 
			           <th title="Configuration used by ${tool}" colspan="${pna}">${name}
	               </#list>
               </#list>
		   </tr>
		   <tr>
			   <#list tools as tool>
			       <#list resultfolders as resultfolder>
				       <th title="Clones found by all tools">+
				       <th title="Clones found by only by ${tool}">-
				       <th title="All clones found by ${tool}">*
	               </#list>
               </#list>
		   </tr>
    	   <#items as repository>
		  <tr>
	        	<td>${repository?index+1}
	        	<td>${repository.name} 
                <#list values[repository?index] as value>
                    <#assign tool=tools[(value?index/(resultfolders?size*tools?size))?int]>
					<#if (value?index%2 == 0)>
						<#assign htmlfile="+.html">
					<#else>
						<#assign htmlfile="-.html">
					</#if>                	 
	        	     <td><a href="${resultfolders[((value?index%(resultfolders?size*tools?size))/(2))?int].name}/${repository.name}/${tool}${htmlfile}">${value}</a>
					<#if (value?index%2 == 1)>
	        	       <td><a href="${resultfolders[((value?index%(resultfolders?size*tools?size))/(2))?int].name}/${repository.name}/${tool}.html">${value+values[repository?index][value?index-1]}</a>
					</#if>
	            </#list>
		  </tr>
    		</#items>
  		</table>
  		</div>
	<#else>
	    No repositories processed!
	</#list>
 	<br><b>Second header row shows the tool configuration used:</b>
 	<br>McSheep configuration is [x-y] where x is the minimum number of total coincident calls between each clone snippet and y is the minimum number of continuous coincident calls between each clone snippet.
 	<br>PMD configuration is the number of tokens.
 	<br><br><b>Third header row indicates how the code clones are grouped regarding agreement between both tools:</b>
 	<br>+ indicates the code clones found by both tools.
 	<br>- indicates the code clones found only by the tool.
 	<br>* indicates all code clones found by the tool.
</body>
</html>