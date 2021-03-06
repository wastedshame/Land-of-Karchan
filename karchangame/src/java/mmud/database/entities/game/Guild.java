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
package mmud.database.entities.game;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import mmud.database.entities.characters.Person;
import mmud.exceptions.MudException;
import mmud.rest.services.GuildBean;
import org.hibernate.annotations.Filter;

/**
 *
 * @author maartenl
 */
@Entity
@Table(name = "mm_guilds", catalog = "mmud", schema = "")
@NamedQueries(
{
    @NamedQuery(name = "Guild.findAll", query = "SELECT g FROM Guild g ORDER BY g.title"),
    @NamedQuery(name = "Guild.findByName", query = "SELECT g FROM Guild g WHERE g.name = :name"),
    @NamedQuery(name = "Guild.findByTitle", query = "SELECT g FROM Guild g WHERE g.title = :title"),
    @NamedQuery(name = "Guild.findByDaysguilddeath", query = "SELECT g FROM Guild g WHERE g.daysguilddeath = :daysguilddeath"),
    @NamedQuery(name = "Guild.findByMaxguilddeath", query = "SELECT g FROM Guild g WHERE g.maxguilddeath = :maxguilddeath"),
    @NamedQuery(name = "Guild.findByMinguildmembers", query = "SELECT g FROM Guild g WHERE g.minguildmembers = :minguildmembers"),
    @NamedQuery(name = "Guild.findByMinguildlevel", query = "SELECT g FROM Guild g WHERE g.minguildlevel = :minguildlevel"),
    @NamedQuery(name = "Guild.findByGuildurl", query = "SELECT g FROM Guild g WHERE g.guildurl = :guildurl"),
    @NamedQuery(name = "Guild.findByActive", query = "SELECT g FROM Guild g WHERE g.active = :active"),
    @NamedQuery(name = "Guild.findByCreation", query = "SELECT g FROM Guild g WHERE g.creation = :creation"),
    @NamedQuery(name = "Guild.findGuildHopefuls", query = "SELECT p from Person p, Charattribute c WHERE c.charattributePK.name = :attributename and c.value = :guildname and c.valueType = :valuetype and c.person = p")
})
public class Guild implements Serializable, DisplayInterface
{

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "name")
    private String name;
    @Size(max = 100)
    @Column(name = "title")
    private String title;
    @Column(name = "daysguilddeath")
    private Integer daysguilddeath;
    @Column(name = "maxguilddeath")
    private Integer maxguilddeath;
    @Column(name = "minguildmembers")
    private Integer minguildmembers;
    @Column(name = "minguildlevel")
    private Integer minguildlevel;
    @Lob
    @Size(max = 65535)
    @Column(name = "guilddescription")
    private String guilddescription;
    @Size(max = 255)
    @Column(name = "guildurl")
    private String guildurl;
    @Basic(optional = false)
    @NotNull
    @Column(name = "active")
    private Boolean active;
    @Basic(optional = false)
    @Column(name = "creation")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creation;
    @Lob
    @Size(max = 65535)
    @Column(name = "logonmessage")
    private String logonmessage;
    @OneToMany(mappedBy = "guild")
    @Filter(name = "activePersons")
    private Collection<Person> members;
    @JoinColumn(name = "owner", referencedColumnName = "name")
    @ManyToOne
    private Admin owner;
    @JoinColumn(name = "bossname", nullable = false, referencedColumnName = "name")
    @ManyToOne(optional = false)
    private Person boss;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "guild")
    private Collection<Guildrank> guildrankCollection;

    public Guild()
    {
    }

    public Guild(String name)
    {
        this.name = name;
    }

    public Guild(String name, Boolean active, Date creation)
    {
        this.name = name;
        this.active = active;
        this.creation = creation;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getTitle()
    {
        return title;
    }

    @Override
    public String getMainTitle()
    {
        return getTitle();
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public Integer getDaysguilddeath()
    {
        return daysguilddeath;
    }

    public void setDaysguilddeath(Integer daysguilddeath)
    {
        this.daysguilddeath = daysguilddeath;
    }

    public Integer getMaxguilddeath()
    {
        return maxguilddeath;
    }

    public void setMaxguilddeath(Integer maxguilddeath)
    {
        this.maxguilddeath = maxguilddeath;
    }

    public Integer getMinguildmembers()
    {
        return minguildmembers;
    }

    public void setMinguildmembers(Integer minguildmembers)
    {
        this.minguildmembers = minguildmembers;
    }

    public Integer getMinguildlevel()
    {
        return minguildlevel;
    }

    public void setMinguildlevel(Integer minguildlevel)
    {
        this.minguildlevel = minguildlevel;
    }

    public String getDescription()
    {
        return guilddescription;
    }

    public void setDescription(String guilddescription)
    {
        this.guilddescription = guilddescription;
    }

    public String getHomepage()
    {
        return guildurl;
    }

    public void setHomepage(String guildurl)
    {
        this.guildurl = guildurl;
    }

    public Boolean getActive()
    {
        return active;
    }

    public void setActive(Boolean active)
    {
        this.active = active;
    }

    public Date getCreation()
    {
        return creation;
    }

    public void setCreation(Date creation)
    {
        this.creation = creation;
    }

    public String getLogonmessage()
    {
        return logonmessage;
    }

    public void setLogonmessage(String logonmessage)
    {
        this.logonmessage = logonmessage;
    }

    public Collection<Person> getMembers()
    {
        return members;
    }

    public void setMembers(Collection<Person> personCollection)
    {
        this.members = personCollection;
    }

    public Admin getOwner()
    {
        return owner;
    }

    public void setOwner(Admin owner)
    {
        this.owner = owner;
    }

    /**
     * Returns the guildmaster of this guild.
     * @return a Person, the guildmaster. Should not be null, ...ever.
     */
    public Person getBoss()
    {
        return boss;
    }

    /**
     * Sets the guildmaster of this guild.
     * @param boss the new guildmaster. Should never be null.
     * @throws MudException if new guildmaster is null, or not a member of this guild.
     */
    public void setBoss(Person boss) throws MudException
    {
        if (boss == null)
        {
            throw new MudException("There should always be a guildmaster of guild " + getName());
        }
        if (boss.getGuild() == null
                || (!boss.getGuild().getName().equals(
                getName())))
        {
            throw new MudException(boss.getName() + " cannot be a guildmaster of guild " + getName());
        }
        this.boss = boss;
    }

    public Collection<Guildrank> getGuildrankCollection()
    {
        return guildrankCollection;
    }

    public void setGuildrankCollection(Collection<Guildrank> guildrankCollection)
    {
        this.guildrankCollection = guildrankCollection;
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
        if (!(object instanceof Guild))
        {
            return false;
        }
        Guild other = (Guild) object;
        if ((this.name == null && other.name != null) || (this.name != null && !this.name.equals(other.name)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "mmud.database.entities.game.Guild[ name=" + name + " ]";
    }

    /**
     * Returns a description in case the guild is in danger of being purged from
     * the database. This is necessary to make sure there is not a plethora of
     * guilds with few members.
     * <P>
     * The following preconditions are in effect:
     * <UL>
     * <LI>the guild is active
     * <LI>there are too few guildmembers (minguildmembers > amountofmembers)
     * </UL>
     * TODO : remove html tags here, let it return a  daysguilddeath or something
     * @return String indicating if there are too few members, usually should be
     *         the empty string if all is well.
     */
    public String getAlarmDescription()
    {
        if ((!getActive()) || (getMinguildmembers() <= getMembers().size()))
        {
            return "";
        }
        return "The minimum amount of members of this guild is "
                + getMinguildmembers()
                + ".<br/>The guild has too few members, and will be removed in "
                + getDaysguilddeath() + " days.";
    }

    /**
     * Searches and returns the person by name in this guild, or null
     * if not found.
     * @param name the name of the person to look for.
     * @return a Person.
     */
    public Person getMember(String name)
    {
        for (Person person : members)
        {
            if (person.getName().equalsIgnoreCase(name))
            {
                return person;
            }
        }
        return null;
    }

    /**
     * communication method to everyone in the guild, that is active.
     * The message is not
     * parsed. Bear in mind that this method should only be used for
     * communication about environmental issues. If the communication originates
     * from a User/Person, you should use sendMessage(aPerson, aMessage).
     * Otherwise the Ignore functionality will be omitted.
     *
     * @param aMessage
     *            the message
     * @throws MudException
     *             if the room is not correct
     * @see Person#writeMessage(java.lang.String)
     */
    public void sendMessage(String aMessage)
            throws MudException
    {
        for (Person myChar : members)
        {
            myChar.writeMessage(aMessage);
        }
    }

    /**
     * character communication method to everyone in the guild. The message is
     * parsed, based on who is sending the message.
     *
     * @param aPerson
     *            the person who is the source of the message.
     * @param aMessage
     *            the message
     *
     * @see Person#writeMessage(mmud.database.entities.characters.Person, java.lang.String)
     */
    public void sendMessage(Person aPerson, String aMessage) throws MudException
    {
        for (Person myChar : members)
        {
            myChar.writeMessage(aPerson, aMessage);
        }
    }

    @Override
    public String getImage()
    {
        // TODO
        return "";
    }

    /**
     * Returns the rank based on the rank id. In case the rank id does not
     * exist, an Exception will be thrown.
     */
    public Guildrank getRank(int id)
    {
        for (Guildrank rank : guildrankCollection)
        {
            if (rank.getGuildrankPK().getGuildlevel() == id)
            {
                return rank;
            }
        }
        return null;

    }

    /**
     * This is primarily used for displaying the current status of the guild to
     * a member of the guild.
     * @return a string containing the description of the guild.
     */
    @Override
    public String getBody() throws MudException
    {
        StringBuffer result = new StringBuffer();
        if (getLogonmessage() != null)
        {
            result.append("<B>Logonmessage:</B>").append(getLogonmessage()).append("<P>");
        }
        result.append("<B>Members</B><P><TABLE>");
        for (Person character : members)
        {
            result.append("<TR><TD>");
            if (character.isActive())
            {
                result.append("<B>");
            }
            result.append(character.getName());
            result.append("</TD><TD>");
            if (character.getAttribute("guildrank") != null)
            {
                Attribute attrib = character.getAttribute("guildrank");
                int title = Integer.parseInt(attrib.getValue());
                Guildrank rank = getRank(title);
                if (rank == null)
                {
                    result.append(" Initiate");
                } else
                {
                    result.append(" ").append(rank.getTitle());
                }
            } else
            {
                result.append(" Initiate");
            }
            if (character.isActive())
            {
                result.append("</B>");
            }
            result.append("</TD></TR>");
        }
        result.append("</TABLE><P><B>Hopefuls</B><BR>");
        // TODO
        GuildBean guildBean;
        try
        {
            guildBean = (GuildBean) new InitialContext().lookup("java:module/GuildBean");

        } catch (NamingException e)
        {
            throw new MudException("Unable to retrieve guild.", e);
        }
        for (Person hopefull : guildBean.getGuildHopefuls(this))
        {
            result.append(hopefull.getName());
            result.append(" ");
        }
        result.append("<P><B>Guildranks</B><BR>");
        for (Guildrank rank : guildrankCollection)
        {
            result.append(rank.getTitle()).append("(").append(rank.getGuildrankPK().getGuildlevel()).append(")<BR>");
        }
        // TODO
        // The guild is <I>" + (isActive() ? "active" : "inactive") + "</I>.<BR>
        return "<H1><IMG SRC=\"/images/gif/money.gif\">" + getTitle()
                + "</H1>You" + " are a member of the <I>" + getTitle()
                + "</I> (" + getName() + ").<BR>The current guildmaster is <I>"
                + getBoss().getName() + "</I>.<BR>The guild has " + members.size()
                + " members." + getAlarmDescription() + "<P>" + result + "<P>";

    }
}
