package com.sudo.gehaltor.pdf.data;

public interface Data_Extractor {

    /**
     * This method is used to extract current page title.
     *
     * @return String This returns PDF (Page) Title.
     */
    String getPageTitle();

    /**
     * This method is used to extract current page date (month & year) in form: MMMM_yyyy
     * <p>
     * <br>Example: Juli_2022
     * </p>
     *
     * @return String This returns (Page's) date as a string.
     */
    String getDate();

    /**
     * This method is used to check if date (month & year) in form: MMMM_yyyy is valid.
     * <p>
     * <br>Example: "Juli_2022"
     * <br>returns true
     * </p>
     *
     * @param dateString This is date (in a string form) to be checked if valid.
     * @return Boolean This returns true if date is valid.
     */
    boolean isDateValid(String dateString);

    /**
     * This method is used to retrieve employee's social security number. (ss_number)
     * <p>
     * <br>Example: 1234567890 -> (on page written as: 1234-567890)
     * </p>
     *
     * @return long This returns social security number.
     */
    long getEmployeeSSNumber();

    /**
     * This method is used to retrieve employee's name.
     * <p>
     * <br>Example: DN: 1 <font color="green">Name</font> Surname, Address-Street House number, Country - Postal number City
     * </p>
     *
     * @return String This returns employee's name -> <font color="green">Name</font>.
     */
    String getEmployeeName();

    /**
     * This method is used to retrieve employee's surname.
     * <p>
     * <br>Example: DN: 1 Name <font color="green">Surname</font>, Address-Street House number, Country - Postal number City
     * </p>
     *
     * @return String This returns employee's surname -> <font color="green">Surname</font>.
     */
    String getEmployeeSurname();

    /**
     * This method is used to retrieve employee's full name.
     * <p>
     * <br>Example: DN: 1 <font color="green">Name Surname</font>, Address-Street House number, Country - Postal number City
     * </p>
     *
     * @return String This returns employee's full name -> <font color="green">Name Surname</font>.
     */
    String getEmployeeNameAndSurname();
}
