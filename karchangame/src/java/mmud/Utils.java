/*
 *  Copyright (C) 2012 maartenl
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package mmud;

import org.owasp.validator.html.AntiSamy;
import org.owasp.validator.html.CleanResults;
import org.owasp.validator.html.Policy;

/**
 *
 * @author maartenl
 */
public class Utils
{

    private static final String POLICY_FILE_LOCATION = "/usr/share/glassfish3/glassfish/domains/domain1/config/antisamy-myspace-1.4.4.xml";

    /**
     * Returns a safe string, containing no javascript at all.
     *
     * @param dirtyInput the original string.
     * @return the new string, sanse javascript.
     */
    public static String security(String dirtyInput)
            throws Exception
    {
        Policy policy = Policy.getInstance(POLICY_FILE_LOCATION);

        AntiSamy as = new AntiSamy();

        CleanResults cr = as.scan(dirtyInput, policy);
        return cr.getCleanHTML(); // some custom function
    }

    /**
     * Returns a safe string, containing only alphabetical characters and space.
     *
     * @param value the original string.
     * @return the new string
     */
    public static String alphabeticalandspace(String value)
            throws Exception
    {
        return value.replaceAll("[^A-Za-z ]", "");
    }

    /**
     * Returns a safe string, containing only alphabetical characters.
     *
     * @param value the original string.
     * @return the new string
     */
    public static String alphabetical(String value)
            throws Exception
    {
        return value.replaceAll("[^A-Za-z]", "");
    }

    /**
     * Returns a safe string, containing only alphanumerical characters and
     * space.
     *
     * @param value the original string.
     * @return the new string
     */
    public static String alphanumericalandspace(String value)
            throws Exception
    {
        return value.replaceAll("[^A-Za-z0-9 ]", "");
    }

    /**
     * Returns a safe string, containing only alphanumerical characters and
     * punctuation.
     *
     * @param value the original string.
     * @return the new string
     */
    public static String alphanumericalandpuntuation(String value)
            throws Exception
    {
        return value.replaceAll("[^A-Za-z0-9!&()_=+;:.,?'\"\\- ]", "");
    }

    /**
     * Returns a safe string, containing only alphanumerical characters.
     *
     * @param value the original string.
     * @return the new string
     */
    public static String alphanumerical(String value)
            throws Exception
    {
        return value.replaceAll("[^A-Za-z0-9]", "");
    }
}
