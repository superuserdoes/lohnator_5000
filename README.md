<h1 align="center">
  <br>  
  <img src="https://raw.githubusercontent.com/superuserdoes/lohnator_5000/main/src/main/resources/com/sudo/lohnator_5000/logo/logo.png" alt="Lohnator 5000" width="200">
  <br>
  Lohnator 5000
  <br>
</h1>
 
<h4 align="center">A multithreaded desktop app automating extraction of paychecks from a multi-page financial report PDF file created by a financial advisor, optionally printing and sending them via e-mail.</h4>

<p align="center"> •
  <a href="#key-features">Key Features</a> •
  <a href="#usage">Usage</a> •
  <a href="#how-to-use">How To Use</a> •  
</p>

![screenshot](https://github.com/superuserdoes/lohnator_5000/raw/main/src/main/resources/com/sudo/lohnator_5000/gif/mainwindow.gif?raw=true)

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

# Usage

[(Back to top)](#key-features)

Showing all parts of the App.

### First Login into your E-Mail account
- If you want to synchronize your PDF files automatically.
- Search for sender messages by:
  - E-Mail Adress 
  - Name (and/or) Surname
  - Downloads only `.pdf` attachments.

  ![image](https://github.com/superuserdoes/lohnator_5000/raw/main/src/main/resources/com/sudo/lohnator_5000/gif/login_email.gif?raw=true)

### Live Processing of `PDF` files
- Instantly see which files are being processed and the result.

  ![image](https://github.com/superuserdoes/lohnator_5000/raw/main/src/main/resources/com/sudo/lohnator_5000/gif/processing_files.gif?raw=true)

### Add new or delete old `PDF` files
- ADD FILE(s): Click on `File` then `new File` to add new files.
- DELETE: Manually select which files you want to delete by right-clicking on them and selecting `Delete`. 

  ![image](https://github.com/superuserdoes/lohnator_5000/raw/main/src/main/resources/com/sudo/lohnator_5000/gif/delete_add_files.gif?raw=true)


### Update `Employee` information
- Select desired `Employee` and right-click on `Update info` then on the right side of the window update:
  - `Name`
  - `Surname`
  - `E-Mail`

  ![image](https://github.com/superuserdoes/lohnator_5000/raw/main/src/main/resources/com/sudo/lohnator_5000/gif/update_employee_email.gif?raw=true)

### Print `PDF`
- `SINGLE PDF file:` 
  - Select desired `.pdf file` and:
    - right-click on `Print` or 
    - click on `Print PDF(s)` button at the bottom
    - and then on the new `Printer Settings` window optionally configure all print settings as well as the right printer (device).
        ![image](https://github.com/superuserdoes/lohnator_5000/raw/main/src/main/resources/com/sudo/lohnator_5000/gif/print_single.gif?raw=true)
- `MULTIPLE PDF files:`
  - Check desired `.pdf files` and click on `Print PDF(s)` then on the new `Printer Settings` window optionally configure all print settings as well as the right printer (device).
    ![image](https://github.com/superuserdoes/lohnator_5000/raw/main/src/main/resources/com/sudo/lohnator_5000/gif/print_multiple.gif?raw=true)

### Send `PDF`
- `SINGLE PDF file:`
    - Select desired `.pdf file` and:
        - right-click on `Print` or
        - click on `Send PDF(s) via E-Mail` button at the bottom
        - and then on the new window optionally configure the E-Mail message and click on the `Send` button.
- `MULTIPLE PDF files:`  
  - Check desired `.pdf files` and click on `Send PDF(s) via E-Mail` button and then on the new window optionally configure the E-Mail message and click on the `Send` button.
- `Attachments` button is for:
  - Adding new attachments
  - Deleting already added attachments 

  ![image](https://github.com/superuserdoes/lohnator_5000/raw/main/src/main/resources/com/sudo/lohnator_5000/gif/send_mail.gif?raw=true)

### Application Settings
- `Performance:` 
  - Select desired number of threads to be used by the program using slider. 
    - **(Depending on your CPU, the number may vary!)**
- `E-Mail login information`: 
  - Enter your credentials if you want to connect to your E-Mail account. (Used for synchronization.)
    - If you want your `password` to be saved using AES Password-Based encryption and decryption, select `remember`. 
- `Financial advisor information:` 
  - Save financial advisor's information , either `name` or `e-mail` for convenience.

  ![image](https://github.com/superuserdoes/lohnator_5000/raw/main/src/main/resources/com/sudo/lohnator_5000/gif/settings.gif?raw=true)






## How To Use

[(Back to top)](#key-features)

To clone and run this application, you'll need [Git](https://git-scm.com) installed on your computer. From your command line:

```bash
# Clone this repository
$ git clone https://github.com/superuserdoes/lohnator_5000.git

$ cd lohnator_5000
```

> Open the project and navigate to <i><b>Start.java</b></i> and right click <b><i>Run 'Start.main()</b></i>'.
> </br>**Note:**
> **IF you don't have Git installed, just manually download by clicking on <b><i><>Code</b></i> and then <b><i>Download ZIP</b></i>**.
