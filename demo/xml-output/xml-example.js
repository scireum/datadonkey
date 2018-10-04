// example xml output
var out = outputXMLWithDoctype("demo/xml-output/xml-example.xml", "some_system.dtd");
out.beginResult("root-element");

out.beginObject("HEADER");
out.property("title", "my xml");
out.endObject();

// fluent call
out.beginObject("CONTENT")
// text-tag with attribute
    .beginObject("paragraph", xmlAttribute("type", "text"))
    .text("This is a paragraph")
    .endObject()
    .endObject();

out.endResult();
