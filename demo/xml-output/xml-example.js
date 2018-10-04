// example xml output
var out = outputXMLWithDoctype("demo/xml-output/xml-example.xml", "some_system.dtd");
out.xml().beginResult("root-element");

out.xml().beginObject("HEADER");
out.xml().property("title", "my xml");
out.xml().endObject();

// fluent call
out.xml().beginObject("CONTENT")
// text-tag with attribute
    .beginObject("paragraph", out.attr("type", "text"))
    .text("This is a paragraph")
    .endObject()
    .endObject();

out.xml().endResult();
