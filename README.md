# DataDonkey

Handles XML, CSV and Excel file.

DataDonkey is an OpenSource project by scireum GmbH. It is a scriptable ETL tool which processes various files.

Download the latest datadonkey.jar from: https://oss.sonatype.org/content/groups/public/com/scireum/datadonkey/

## Example

See demo/example.js for an example script which combines and XML source (demo/example.xml) with a
CSV file (demo/example.csv) and generates an Excel output.

## Reference

* All relevant functions are defined in [donkey.js](/blob/master/src/main/resources/donkey.js)
* XML nodes are presented as [StructuredNode](http://sirius-lib.net/apidocs/sirius-kernel/sirius/kernel/xml/StructuredNode.html)
* Rows of Excel or CVS files are represented as: [Values](http://sirius-lib.net/apidocs/sirius-kernel/sirius/kernel/commons/Values.html)
* **queryValue(String)** of StructuredNode and **at(String)** of Values return: [Value](http://sirius-lib.net/apidocs/sirius-kernel/sirius/kernel/commons/Value.html)

## License

DataDonkey is licensed under the MIT License:

> Permission is hereby granted, free of charge, to any person obtaining a copy
> of this software and associated documentation files (the "Software"), to deal
> in the Software without restriction, including without limitation the rights
> to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
> copies of the Software, and to permit persons to whom the Software is
> furnished to do so, subject to the following conditions:
> 
> The above copyright notice and this permission notice shall be included in
> all copies or substantial portions of the Software.
> 
> THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
> IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
> FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
> AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
> LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
> OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
> THE SOFTWARE.

