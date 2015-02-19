// In this scenario we want to merge two files into one output file:
//
// example.csv - contains item numbers along the their prices
// example.xml - contains item numbers along with a short description of the item
//
// We now want to export an Excel file containing the item number, the description and the price of
// each item.

// We will load example.xml into this map. Using the item number as key and the shorttext as value
var articleTextMap = {};

// Defines all node handler used to parse the xml
// In this case we want to handle each <item> tag...
var parserMap = {
    'item' : function(node) {
        // node is of type sirius.kernel.xml.StructuredNode and provides queryString and queryValue which both
        // evaluate XPATH expressions against the sub DOM (an item tag in this case)
        articleTextMap[node.queryString('number')] = node.queryString('shorttext');
    }
}

// Invokes the XML parser. As this is mainly a SAX parser which only extracts the requested sub DOMs
// we can process very large XML files using this approach...
parseXML('demo/example.xml', parserMap);

// As a result we want to generate an Excel file containing the item number, shorttext and the price
var out = outputExcel();

// Parse the example.csv which contains the item number along with its price
inputFile('demo/example.csv', function(line, row) {
    // Invoked per row. The row object is of type scireum.kernel.commons.Values
    // The at function accepts an "Excel style" column number and returns a scireum.kernel.commons.Value
    // which helps to convert the data into all kinds of types
    var itemNumber = row.at('A').asString();
    var shorttext = articleTextMap[itemNumber];
    var price = row.at('B').getAmount();

    // Write to excel
    out.addRow(itemNumber, shorttext, price.getAmount());

    // Log some info to the console
    log(itemNumber+': "' + shorttext + '" Price: '+price.toString());
});

// Save the result as xls file...
out.save('demo/example.xls');


