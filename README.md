<h1 align="center">
  <br>  
  <img src="https://raw.githubusercontent.com/superuserdoes/gehaltor_5000/main/src/main/resources/com/sudo/gehaltor/logo/logo.png" alt="Lohnator 5000" width="200">
  <br>
  Lohnator 5000
  <br>
</h1>

<h4 align="center">A desktop app automating extraction of paychecks from a multi-page financial report PDF file created by a financial advisor.</h4>

<p align="center"> •
  <a href="#key-features">Key Features</a> •
  <a href="#how-to-use">How To Use</a> •  
</p>

![screenshot](https://github.com/superuserdoes/gehaltor_5000/blob/21f04d930f006a69d6458577d9860409ba95a70f/src/main/resources/com/sudo/gehaltor/gif/mainwindow.gif?raw=true)

## Key Features

* Live Processing - Add file(s), See change(s)
    - Instantly see which files are being processed and the result.
* Sync Files
    - Downloads automatically the newest files from the specified E-Mail account.
* Save E-Mail credentials (optional)
   - AES Password-Based encryption and decryption.
* Support for PDF files with (in)correct Encoding value(s). Specifically, PDF's resources:
    -  Fonts encoding:
       - WinAnsiEncoding and
       - Identity-H encoding
  - (in)complete ToUnicode map(ping) 
* OCR (Optical Character Recognition) support
  - Using Tesseract via tess4j to extract the text from embedded images.
* Print single or multiple PDF file(s)
* Send single or multiple PDF file(s) via E-Mail
* Resizable Window
* Multithreaded
   - Number of threads can be manually adjusted in Settings.
* Cross-platform
    - Windows, macOS and Linux ready.

## How To Use

To clone and run this application, you'll need [Git](https://git-scm.com) installed on your computer. From your command line:

```bash
# Clone this repository
$ git clone https://github.com/superuserdoes/gehaltor_5000.git

$ cd gehaltor_5000
```

> Open the project and navigate to <i><b>Start.java</b></i> and right click <b><i>Run 'Start.main()</b></i>'.
> </br>**Note:**
> **IF you don't have Git installed, just manually download by clicking on <b><i>Download ZIP</b></i>**.