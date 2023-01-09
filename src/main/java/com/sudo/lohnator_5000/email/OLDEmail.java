package com.sudo.lohnator_5000.email;

import com.sudo.lohnator_5000.config.AppConfiguration;
import com.sudo.lohnator_5000.data.AppSettings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class OLDEmail {

    private static EmailConfig emailConfig;
    private String search_from;
    private Set<String> found_email_addresses = null;
    private final String download_directory = AppConfiguration.DOWNLOADS_PATH.getValue();
    // -------------------------------------------------
    // online settings
    // -------------------------------------------------
    private String mbox = "INBOX";
    private boolean saveAttachments = false;
    private String file_type = ".pdf";
    private int num_of_saved_files = 0;
    private boolean or = true;
    private int level = 0;
    //--------------------------------------------------
    private int num_of_messages = 0;
    private LinkedHashSet<File> downloaded_attachments = new LinkedHashSet<>();
    private final StringProperty text_label = new SimpleStringProperty();
    private Task check_email_task;

    // -------------------------------------------------

    public Task get_check_email_task() {
        return check_email_task;
    }

    public ArrayList<File> get_downloaded_attachments(){
        ArrayList<File> attachments = new ArrayList<>();
        attachments.addAll(downloaded_attachments);
        System.out.println();
        System.out.println();
        System.out.println();

        System.out.println("File(s): " + attachments.size());
        for (File file : attachments){
            System.out.println("*********-> " + file.getName());
        }

        System.out.println();
        System.out.println();
        System.out.println();
        return attachments;
    }

    public void setText_label(String text_label) {
        this.text_label.set(text_label);
    }

    public String getText_label() {
        return text_label.get();
    }

    public StringProperty text_labelProperty() {
        return text_label;
    }

    public OLDEmail() throws MessagingException {
        load_saved_properties();
    }

    private static void load_saved_properties(){
        try {
            emailConfig = new EmailConfig(AppSettings.getInstance().getEmail(), AppSettings.getInstance().getPassword());
        } catch (Exception ex) {}
    }

    public OLDEmail(EmailConfig emailConfig, String name, boolean downloadAttachments) throws MessagingException {
        this.emailConfig = emailConfig;
        if (name == null || name.isEmpty() || name.length() <=2)
            System.out.println("Name is too short (<=2) OR empty!");
        try { check_name(name, downloadAttachments); } catch (MessagingException e) { e.printStackTrace(); }
    }

    public OLDEmail(String search_name) {
        search_from = search_name;
        search(null,search_name,false);
    }

    public OLDEmail(String name, boolean downloadAttachments) {
        if (name == null || name.isEmpty() || name.length() <=2)
            System.out.println("Name is too short (<=2) OR empty!");
        try { check_name(name, downloadAttachments); } catch (MessagingException e) { e.printStackTrace(); }
    }

    // either -subject, -from, -today
    public void search(String subject, String from, boolean today){
        found_email_addresses = new HashSet<>();
        if ((subject == null) && (from == null) && !today) {
            System.out.println("Specify either -subject, -from, -today, or -size");
            System.exit(1);
        }
        try {
//                    updateMessage("Trying to open INBOX folder...");
            // Open the Folder
            Folder folder = emailConfig.get_store().getDefaultFolder();
            if (folder == null) {
//                        updateMessage("Cant find default namespace");
                System.out.println("Cant find default namespace");
                System.exit(1);
            }

            folder = folder.getFolder(mbox);
            if (folder == null) {
//                        updateMessage("Invalid folder");
                System.out.println("Invalid folder");
                System.exit(1);
            }

            folder.open(Folder.READ_ONLY);
            SearchTerm term = null;

            if (subject != null)
                term = new SubjectTerm(subject);
            if (from != null) {
                FromStringTerm fromTerm = new FromStringTerm(from);
                if (term != null) {
                    if (or)
                        term = new OrTerm(term, fromTerm);
                    else
                        term = new AndTerm(term, fromTerm);
                }
                else
                    term = fromTerm;
            }
            if (today) {
                Calendar c = Calendar.getInstance();
                c.set(Calendar.HOUR, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
                c.set(Calendar.AM_PM, Calendar.AM);

                ReceivedDateTerm startDateTerm = new ReceivedDateTerm(ComparisonTerm.GE, c.getTime());
                c.add(Calendar.DATE, 1);	// next day
                ReceivedDateTerm endDateTerm = new ReceivedDateTerm(ComparisonTerm.LT, c.getTime());

                SearchTerm dateTerm = new AndTerm(startDateTerm, endDateTerm);
                if (term != null) {
                    if (or)
                        term = new OrTerm(term, dateTerm);
                    else
                        term = new AndTerm(term, dateTerm);
                }
                else
                    term = dateTerm;
            }

            Message[] msgs = folder.search(term);
            num_of_messages = msgs.length;
            System.out.println("FOUND " + num_of_messages + " MESSAGES");
//                    updateMessage("FOUND " + num_of_messages + " MESSAGES");
            if (msgs.length == 0) // no match
                System.exit(1);

            // Use a suitable FetchProfile
            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.ENVELOPE);
            folder.fetch(msgs, fp);

            for (int i = 0; i < msgs.length; i++) {
//                System.out.println("--------------------------");
//                System.out.println("MESSAGE #" + (i + 1) + ":");
//                dumpPart(msgs[i]);

                Address[] froms = msgs[i].getFrom();
                String email = froms == null ? null : ((InternetAddress) froms[0]).getAddress();
//                System.out.println(email);
                found_email_addresses.add(email);
            }

            System.out.println("--------------------------------");
            for (String email : found_email_addresses)
                System.out.println(email);

//            System.out.println("Number of " + file_type + " files: " + num_of_saved_files);
//                    updateMessage("Number of " + file_type + " files: " + num_of_files);

            folder.close(false);
//            store.close();
        } catch (Exception ex) {
//                    updateMessage("Oops, got exception! " + ex.getMessage());
            System.out.println("Oops, got exception! " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void search_name(String person_name, boolean downloadAttachments) throws MessagingException, IOException {
        System.out.println("YOOOOOOOOOOOO");
        StringBuilder output_text = new StringBuilder();

        System.out.println("YOOOOOOOOOOOO 11111111111");

        if (person_name == null || person_name.isEmpty()){
            System.out.println("Field is empty!");
            output_text.append(">Field is empty!\n");
//                    updateMessage(output_text.toString());
//                    return null;
            return;
        }
        search_from = person_name;
        if (!emailConfig.get_store().isConnected()) {
            emailConfig.get_store().connect();
        }
        // open the default folder
        Folder folder = emailConfig.get_store().getDefaultFolder();
        if (!folder.exists()) {
            throw new MessagingException("No default (root) folder available");
        }
        output_text.append("-> Opening INBOX...\n");
        System.out.println("-> Opening INBOX...\n");
//                updateMessage(output_text.toString());
//                updateProgress(1,20);
        // open the inbox
        folder = folder.getFolder("inbox");
        if (!folder.exists()) {
            throw new MessagingException("No INBOX folder available");
        }
        // get the message count; stop if nothing to do
        folder.open(Folder.READ_ONLY);
        int totalMessages = folder.getMessageCount();
        if (totalMessages == 0) {
            output_text.append("-> Total number of e-mails is equal to zero!\nExiting...\n");
//                    updateMessage(output_text.toString());
            folder.close(false);
            throw new IllegalArgumentException("No E-mail was found!");
        }
        output_text.append("-> Total number of e-mails: " + totalMessages + "...\n");
        System.out.println("-> Total number of e-mails: " + totalMessages + "...\n");
//                updateMessage(output_text.toString());
//                updateProgress(2,20);
        // get all messages
//        Message[] messages = folder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
        Message[] messages = folder.search(new FromStringTerm(search_from));
        num_of_messages = messages.length;

        output_text.append("-> Trying to e-mail matching <\"" + search_from + "\">...\n");
        System.out.println("-> Trying to e-mail matching <\"" + search_from + "\">...\n");
//                updateMessage(output_text.toString());
//                updateProgress(3,num_of_messages);

        if (num_of_messages == 0){
            System.out.println("ERROR: NO EMAIL(S) WITH <" + search_from + "> FOUND!");
            output_text.append("-> ERROR: EMAIL <" + search_from + "> NOT FOUND!\n");
//                    updateMessage(output_text.toString());
            folder.close(true);
            throw new IllegalArgumentException("-> ERROR: EMAIL <" + search_from + "> NOT FOUND!\n");
        }


        output_text.append("-> " + num_of_messages + " E-Mail(s) found from <\"" + search_from + "\">...\n");
        System.out.println("-> " + num_of_messages + " E-Mail(s) found from <\"" + search_from + "\">...\n");
//                updateMessage(output_text.toString());
//                updateProgress(4,num_of_messages);
        System.out.println(num_of_messages + " E-Mail(s):");
//        Message[] messages = folder.getMessages();
        output_text.append("-> Fetching " + num_of_messages + " e-mails, please be patient and wait a moment...\n");
//                updateMessage(output_text.toString());
        FetchProfile profile = new FetchProfile();
        profile.add(FetchProfile.Item.ENVELOPE);
        profile.add(FetchProfile.Item.FLAGS);
        profile.add("X-Mailer");
        folder.fetch(messages, profile);
        // process each message
        found_email_addresses = new HashSet<>();

        String name = "";
        String surname = "";
        downloaded_attachments.clear();
        int counter = 0;
        for (Message message: messages) {
            InternetAddress sender_email = (InternetAddress) message.getFrom()[0];
            System.out.println("Sender name: " + sender_email.getPersonal());
            System.out.println("Sender email: " + sender_email.getAddress());

//                    String[] full_name = sender_email.getPersonal().trim().split("\\s+");
//                    name = full_name[0];
//                    surname = full_name[1];
//
//                    System.out.println("NAME:" + name);
//                    System.out.println("SURNAME:" + surname);

            if (downloadAttachments) {
                output_text.append("-> Searching corresponding attachments to be downloaded " + counter  + " / " + num_of_messages + "...\n");
                System.out.println("-> Searching corresponding attachments to be downloaded " + counter  + " / " + num_of_messages + "...\n");
//                        updateMessage(output_text.toString());
                String contentType = message.getContentType();
                if (contentType.contains("multipart")) {
                    // content may contain attachments
                    Multipart multiPart = (Multipart) message.getContent();
                    int numberOfParts = multiPart.getCount();
                    for (int partCount = 0; partCount < numberOfParts; partCount++) {
                        MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
                        if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                            // this part is attachment
                            String filename = part.getFileName();
                            if (filename != null && filename.endsWith(file_type)) {
                                //                                    attachFiles += fileName + ", ";
                                output_text.append("-> Downloading: <" + filename + ">...\n");
                                System.out.println("-> Downloading: <" + filename + ">...\n");
//                                        updateMessage(output_text.toString());

                                // make folder
                                File savedir = new File(download_directory);
                                savedir.mkdirs();
                                // save file into this folder
                                File file = new File(savedir, filename);
                                part.saveFile(file);
                                downloaded_attachments.add(file);

//                                        part.saveFile(filename);
//                                        downloaded_attachments.add(new File(part.getFileName()));
                            }
                        }
                    }
                }
//                        updateProgress(++counter,num_of_messages);
                text_label.setValue("E-Mail: " + counter + " / " + num_of_messages);

            }

            if (!sender_email.getAddress().isEmpty() && !sender_email.getAddress().contains("facebookmail") && !sender_email.getAddress().contains("no-reply") && !sender_email.getAddress().contains("noreply")){
                System.out.println("sender_email NOT EMPTY");
                found_email_addresses.add(sender_email.getAddress());
            } else {
//                        System.out.println("MANUALLY doing shit");
//                        // get e-mail manually if above doesn't work (shouldn't happen)
//                        String from = message.getFrom()[0].toString();
//                        //System.out.println("Message from: " + from);
//                        String from_email = "null";
//                        if (from.contains("<") && from.contains(">") && !sender_email.getAddress().contains("facebookmail") && !sender_email.getAddress().contains("no-reply") && !sender_email.getAddress().contains("noreply")){
//                            from_email = from.substring(from.indexOf("<") + 1, from.indexOf(">")); // or ( x , from.length() )
////                System.out.println("FROM: " + from);
////                System.out.println("FROM_EMAIL: " + from_email);
//                            if (from.toLowerCase().contains(search_from.toLowerCase())){
////                    System.out.println("FROM mail: " + from_email);
//                                found_email_addresses.add(from_email);
//                            }
//                        }
            }


        }

        // expunge and close the folder
        folder.close(true);

        output_text.append("-> Getting unique e-mails...\n");
        System.out.println("-> Getting unique e-mails...\n");
//                updateMessage(output_text.toString());
//                updateProgress(5,5);

        output_text.append("-> " + found_email_addresses.size() + " unique e-mail(s):\n");
        System.out.println("-> " + found_email_addresses.size() + " unique e-mail(s):");
//                updateMessage(output_text.toString());
//                updateProgress(6,5);
        for (String email : found_email_addresses){
//                    System.out.println(email);
            output_text.append("\t<" + email + ">\n");
            System.out.println("\t<" + email + ">");
//                    updateMessage(output_text.toString());
        }

    }

    public void send_message_and_or_file(String to, String subject, String text, List<File> files){
        if ((to == null) && (subject == null) && (text == null)) {
            System.out.println("Specify either -subject, -from, -today, or -size");
//            System.exit(1);
            return;
        }
        try {
            // create a message
            MimeMessage msg = new MimeMessage(emailConfig.get_session());
            msg.setFrom(new InternetAddress(emailConfig.getUser()));

            InternetAddress[] address = {new InternetAddress(to)};

            msg.setRecipients(Message.RecipientType.TO, address);
            msg.setSubject(subject);

            // create and fill the first message part
            MimeBodyPart mbp1 = new MimeBodyPart();
            mbp1.setContent(text, "text/html; charset=utf-8");

            // create the Multipart and add its parts to it
            Multipart mp = new MimeMultipart();
            mp.addBodyPart(mbp1);

            for (File file: files) {
                if (file.exists() || file.isFile()){
                    MimeBodyPart messageAttachmentPart = new MimeBodyPart();
                    messageAttachmentPart.attachFile(file.getPath());
                    mp.addBodyPart(messageAttachmentPart);
                }
            }

            // add the Multipart to the message
            msg.setContent(mp);

            // set the Date: header
            msg.setSentDate(new Date());

            // send the message
            Transport.send(msg);
        } catch (MessagingException mex) {
            mex.printStackTrace();
            Exception ex = null;
            if ((ex = mex.getNextException()) != null) {
                ex.printStackTrace();
            }
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
    }

    private void check_name(String name, boolean downloadAttachments) throws MessagingException {
        check_email_task = new Task() {
            StringBuilder output_text = new StringBuilder();

            @Override
            protected Object call() throws Exception {
                System.out.println("YOOOOOOOOOOOO 11111111111");

                if (name == null || name.isEmpty()) {
                    System.out.println("Field is empty!");
                    output_text.append(">Field is empty!\n");
                    updateMessage(output_text.toString());
                    return null;
                }
                search_from = name;
                if (!emailConfig.get_store().isConnected()) {
                    emailConfig.get_store().connect();
                }
                // open the default folder
                Folder folder = emailConfig.get_store().getDefaultFolder();
                if (!folder.exists()) {
                    throw new MessagingException("No default (root) folder available");
                }
                output_text.append("-> Opening INBOX...\n");
                System.out.println("-> Opening INBOX...\n");
                updateMessage(output_text.toString());
//                updateProgress(1,20);
                // open the inbox
                folder = folder.getFolder("inbox");
                if (!folder.exists()) {
                    throw new MessagingException("No INBOX folder available");
                }
                // get the message count; stop if nothing to do
                folder.open(Folder.READ_ONLY);
                int totalMessages = folder.getMessageCount();
                if (totalMessages == 0) {
                    output_text.append("-> Total number of e-mails is equal to zero!\nExiting...\n");
                    updateMessage(output_text.toString());
                    folder.close(false);
                    throw new IllegalArgumentException("No E-mail was found!");
                }
                output_text.append("-> Total number of e-mails: " + totalMessages + "...\n");
                System.out.println("-> Total number of e-mails: " + totalMessages + "...\n");
                updateMessage(output_text.toString());
//                updateProgress(2,20);
                // get all messages
//        Message[] messages = folder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
                Message[] messages = folder.search(new FromStringTerm(search_from));
                num_of_messages = messages.length;

                output_text.append("-> Trying to e-mail matching <\"" + search_from + "\">...\n");
                System.out.println("-> Trying to e-mail matching <\"" + search_from + "\">...\n");
                updateMessage(output_text.toString());
                updateProgress(3, num_of_messages);

                if (num_of_messages == 0) {
                    System.out.println("ERROR: NO EMAIL(S) WITH <" + search_from + "> FOUND!");
                    output_text.append("-> ERROR: EMAIL <" + search_from + "> NOT FOUND!\n");
                    updateMessage(output_text.toString());
                    folder.close(true);
                    throw new IllegalArgumentException("-> ERROR: EMAIL <" + search_from + "> NOT FOUND!\n");
                }

                output_text.append("-> " + num_of_messages + " E-Mail(s) found from <\"" + search_from + "\">...\n");
                System.out.println("-> " + num_of_messages + " E-Mail(s) found from <\"" + search_from + "\">...\n");
                updateMessage(output_text.toString());
                updateProgress(4, num_of_messages);
                System.out.println(num_of_messages + " E-Mail(s):");
//        Message[] messages = folder.getMessages();
                output_text.append("-> Fetching " + num_of_messages + " e-mails, please be patient and wait a moment...\n");
                updateMessage(output_text.toString());
                FetchProfile profile = new FetchProfile();
                profile.add(FetchProfile.Item.ENVELOPE);
                profile.add(FetchProfile.Item.FLAGS);
                profile.add("X-Mailer");
                folder.fetch(messages, profile);
                // process each message
                found_email_addresses = new HashSet<>();
                downloaded_attachments.clear();
                int counter = 1;
                for (Message message : messages) {
                    InternetAddress sender_email = (InternetAddress) message.getFrom()[0];
                    System.out.println("Sender name: " + sender_email.getPersonal());
                    System.out.println("Sender email: " + sender_email.getAddress());
                    if (downloadAttachments) {
                        output_text.append("-> Searching attachments in message " + counter + " / " + num_of_messages + "...\n");
                        System.out.println("-> Searching attachments in message " + counter + " / " + num_of_messages + "...\n");
                        updateMessage(output_text.toString());
                        String contentType = message.getContentType();
                        if (contentType.contains("multipart")) {
                            // content may contain attachments
                            Multipart multiPart = (Multipart) message.getContent();
                            int numberOfParts = multiPart.getCount();
                            for (int partCount = 0; partCount < numberOfParts; partCount++) {
                                MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
                                if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                                    // this part is attachment
                                    String filename = part.getFileName();
                                    if (filename != null && filename.endsWith(file_type)) {
                                        //                                    attachFiles += fileName + ", ";
                                        output_text.append("-> Downloading: <" + filename + ">...\n");
                                        System.out.println("-> Downloading: <" + filename + ">...\n");
                                        updateMessage(output_text.toString());
                                        // make folder
                                        File savedir = new File(download_directory);
                                        savedir.mkdirs();
                                        // save file into this folder
                                        File file = new File(savedir, filename);
                                        part.saveFile(file);
                                        downloaded_attachments.add(file);
//                                        part.saveFile(filename);
//                                        downloaded_attachments.add(new File(part.getFileName()));
                                    }
                                }
                            }
                        }
                        text_label.setValue("E-Mail: " + counter + " of " + num_of_messages);
                        updateProgress(counter++, num_of_messages);
                    }
                    if (!sender_email.getAddress().isEmpty() && !sender_email.getAddress().contains("facebookmail") && !sender_email.getAddress().contains("no-reply") && !sender_email.getAddress().contains("noreply")) {
                        System.out.println("sender_email NOT EMPTY");
                        found_email_addresses.add(sender_email.getAddress());
                    }
                }
//                person = new Employee(name,surname, found_email_addresses.toString());
                // expunge and close the folder
                folder.close(true);

                output_text.append("-> Getting unique e-mails...\n");
                System.out.println("-> Getting unique e-mails...\n");
                updateMessage(output_text.toString());
                updateProgress(5, 5);

                output_text.append("-> " + found_email_addresses.size() + " unique e-mail(s):\n");
                System.out.println("-> " + found_email_addresses.size() + " unique e-mail(s):\n");
                updateMessage(output_text.toString());
                updateProgress(6, 5);
                for (String email : found_email_addresses) {
                    System.out.println(email);
                    output_text.append("\t<" + email + ">\n");
                    System.out.println("\t<" + email + ">\n");
                    updateMessage(output_text.toString());
                }
                return null;
            }
        };
    }

    public void dumpPart(Part p) throws Exception {
        if (p instanceof Message) {
            Message m = (Message)p;
            Address[] a;
            // FROM
            if ((a = m.getFrom()) != null) {
                for (int j = 0; j < a.length; j++)
                    System.out.println("FROM: " + a[j].toString());
            }

            // TO
            if ((a = m.getRecipients(Message.RecipientType.TO)) != null) {
                for (int j = 0; j < a.length; j++)
                    System.out.println("TO: " + a[j].toString());
            }

            // SUBJECT
            System.out.println("SUBJECT: " + m.getSubject());

            // DATE
            Date d = m.getSentDate();
            System.out.println("SendDate: " +
//		(d != null ? d.toLocaleString() : "UNKNOWN"));
                    (d != null ? d.toString() : "UNKNOWN"));

            // FLAGS:
            Flags flags = m.getFlags();
            StringBuffer sb = new StringBuffer();
            Flags.Flag[] sf = flags.getSystemFlags(); // get the system flags

            boolean first = true;
            for (int i = 0; i < sf.length; i++) {
                String s;
                Flags.Flag f = sf[i];
                if (f == Flags.Flag.ANSWERED)
                    s = "\\Answered";
                else if (f == Flags.Flag.DELETED)
                    s = "\\Deleted";
                else if (f == Flags.Flag.DRAFT)
                    s = "\\Draft";
                else if (f == Flags.Flag.FLAGGED)
                    s = "\\Flagged";
                else if (f == Flags.Flag.RECENT)
                    s = "\\Recent";
                else if (f == Flags.Flag.SEEN)
                    s = "\\Seen";
                else
                    continue;	// skip it
                if (first)
                    first = false;
                else
                    sb.append(' ');
                sb.append(s);
            }

            String[] uf = flags.getUserFlags(); // get the user flag strings
            for (int i = 0; i < uf.length; i++) {
                if (first)
                    first = false;
                else
                    sb.append(' ');
                sb.append(uf[i]);
            }
            System.out.println("FLAGS = " + sb.toString());
        }


        String filename = p.getFileName();
        if (filename != null)
            System.out.println("FILENAME: " + filename);

        /*
         * Using isMimeType to determine the content type avoids
         * fetching the actual content data until we need it.
         */
        if (p.isMimeType("multipart/*")) {
            System.out.println("This is a Multipart");
            System.out.println("---------------------------");
            Multipart mp = (Multipart)p.getContent();
            level++;
            int count = mp.getCount();
            for (int i = 0; i < count; i++)
                dumpPart(mp.getBodyPart(i));
            level--;
        } else if (p.isMimeType("message/rfc822")) {
            System.out.println("This is a Nested Message");
            System.out.println("---------------------------");
            level++;
            dumpPart((Part) p.getContent());
            level--;
        }

        /*
         * If we're saving attachments, write out anything that
         * looks like an attachment into an appropriately named
         * file.  Don't overwrite existing files to prevent
         * mistakes.
         */
        if (saveAttachments && level != 0 && p instanceof MimeBodyPart && !p.isMimeType("multipart/*")) {
            String disp = p.getDisposition();
            // many mailers don't include a Content-Disposition
            if (disp == null || disp.equalsIgnoreCase(Part.ATTACHMENT)) {
                if (filename != null && filename.endsWith(file_type)){
                    num_of_saved_files++;
                    try {
                        File f = new File(filename);
                        if (f.exists())
                            // XXX - could try a series of names
                            throw new IOException("file exists");
                        ((MimeBodyPart)p).saveFile(f);
                    } catch (IOException ex) {
                        System.out.println("Failed to save attachment: " + ex);
                    }
                    System.out.println("---------------------------");
                } else
                    System.out.println(filename + " is NOT a " + file_type + " file.");
            }
        }
    }

}
