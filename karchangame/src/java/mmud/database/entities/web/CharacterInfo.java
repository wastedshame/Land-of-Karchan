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
package mmud.database.entities.web;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import mmud.Utils;
import mmud.exceptions.MudException;
import org.apache.commons.validator.routines.UrlValidator;
import org.owasp.validator.html.PolicyException;
import org.owasp.validator.html.ScanException;

/**
 *
 * @author maartenl
 */
@Entity
@Table(name = "characterinfo", catalog = "mmud", schema = "")
@NamedQueries(



{
    @NamedQuery(name = "CharacterInfo.findAll", query = "SELECT c FROM CharacterInfo c"),
    @NamedQuery(name = "CharacterInfo.findByName", query = "SELECT c FROM CharacterInfo c WHERE c.name = :name"),
    @NamedQuery(name = "CharacterInfo.findByImageurl", query = "SELECT c FROM CharacterInfo c WHERE c.imageurl = :imageurl"),
    @NamedQuery(name = "CharacterInfo.findByHomepageurl", query = "SELECT c FROM CharacterInfo c WHERE c.homepageurl = :homepageurl"),
    @NamedQuery(name = "CharacterInfo.findByDateofbirth", query = "SELECT c FROM CharacterInfo c WHERE c.dateofbirth = :dateofbirth"),
    @NamedQuery(name = "CharacterInfo.findByCityofbirth", query = "SELECT c FROM CharacterInfo c WHERE c.cityofbirth = :cityofbirth"),
    @NamedQuery(name = "CharacterInfo.charactersheets", query = "SELECT c.name FROM CharacterInfo c, Person p WHERE c.name = p.name and p.god <= 1"),
})
public class CharacterInfo implements Serializable
{

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "name")
    private String name;
    @Size(max = 255)
    @Column(name = "imageurl")
    private String imageurl;
    @Size(max = 255)
    @Column(name = "homepageurl")
    private String homepageurl;
    @Size(max = 255)
    @Column(name = "dateofbirth")
    private String dateofbirth;
    @Size(max = 255)
    @Column(name = "cityofbirth")
    private String cityofbirth;
    @Lob
    @Size(max = 65535)
    @Column(name = "storyline")
    private String storyline;

    public CharacterInfo()
    {
    }

    public CharacterInfo(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getImageurl()
    {
        return imageurl;
    }

    public void setImageurl(String imageurl) throws MudException
    {
        String[] schemes =
        {
            "http", "https"
        };
        UrlValidator urlValidator = new UrlValidator(schemes);
        if (!urlValidator.isValid(imageurl))
        {
            throw new MudException("imageurl '" + imageurl + "' invalid");
        }
        this.imageurl = imageurl;
    }

    /**
     * An url to the homepage of a player. Only http and https are supported.
     *
     * @return a properly formatted url.
     */
    public String getHomepageurl()
    {
        return homepageurl;
    }

    public void setHomepageurl(String homepageurl) throws MudException
    {
        String[] schemes =
        {
            "http", "https"
        };
        UrlValidator urlValidator = new UrlValidator(schemes);
        if (!urlValidator.isValid(homepageurl))
        {
            throw new MudException("homepageurl '" + homepageurl + "' invalid");
        }
        this.homepageurl = homepageurl;
    }

    public String getDateofbirth()
    {
        return dateofbirth;
    }

    public void setDateofbirth(String dateofbirth)
    {
        if (dateofbirth != null)
        {
            dateofbirth = Utils.alphanumericalandpuntuation(dateofbirth);
        }
        this.dateofbirth = dateofbirth;
    }

    public String getCityofbirth()
    {
        return cityofbirth;
    }

    public void setCityofbirth(String cityofbirth)
    {
        if (cityofbirth != null)
        {
            cityofbirth = Utils.alphanumericalandpuntuation(cityofbirth);
        }
        this.cityofbirth = cityofbirth;
    }

    public String getStoryline()
    {
        return storyline;
    }

    public void setStoryline(String storyline) throws MudException
    {
        if (storyline != null)
        {
            try
            {
                storyline = Utils.security(storyline);
            } catch (    PolicyException | ScanException ex)
            {
                Logger.getLogger(CharacterInfo.class.getName()).log(Level.SEVERE, null, ex);
                throw new MudException(ex);
            }
        }
        this.storyline = storyline;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (name != null ? name.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CharacterInfo))
        {
            return false;
        }
        CharacterInfo other = (CharacterInfo) object;
        if ((this.name == null && other.name != null) || (this.name != null && !this.name.equals(other.name)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "mmud.database.entities.web.CharacterInfo[ name=" + name + " ]";
    }
}
