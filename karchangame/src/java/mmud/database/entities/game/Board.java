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
import java.util.Date;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
import mmud.database.entities.characters.User;
import mmud.exceptions.MudException;

/**
 *
 * @author maartenl
 */
@Entity
@Table(name = "mm_boards", catalog = "mmud", schema = "")
@NamedQueries(
{
    @NamedQuery(name = "Board.findAll", query = "SELECT b FROM Board b"),
    @NamedQuery(name = "Board.findById", query = "SELECT b FROM Board b WHERE b.id = :id"),
    @NamedQuery(name = "Board.findByName", query = "SELECT b FROM Board b WHERE b.name = :name"),
    @NamedQuery(name = "Board.findByCreation", query = "SELECT b FROM Board b WHERE b.creation = :creation")
})
public class Board implements Serializable, DisplayInterface
{

    private static final long serialVersionUID = 1L;
    private static final long ONE_WEEK = 1000l * 60l * 60l * 24l * 7l;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 80)
    @Column(name = "name")
    private String name;
    @Basic(optional = false)
    @NotNull
    @Lob
    @Size(min = 1, max = 65535)
    @Column(name = "description")
    private String description;
    @Basic(optional = false)
    @NotNull
    @Column(name = "creation")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creation;
    @JoinColumn(name = "owner", referencedColumnName = "name")
    @ManyToOne(optional = false)
    private Admin owner;
    @JoinColumn(name = "room", nullable = false, referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @NotNull
    private Room room;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "board", fetch = FetchType.LAZY)
    // TODO : add a where statement here to limit the amount of message to last week max.
    private Set<BoardMessage> messages;

    public Board()
    {
    }

    public Board(Integer id)
    {
        this.id = id;
    }

    public Board(Integer id, String name, String description, Date creation)
    {
        this.id = id;
        this.name = name;
        this.description = description;
        this.creation = creation;
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Date getCreation()
    {
        return creation;
    }

    public void setCreation(Date creation)
    {
        this.creation = creation;
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
     * The rooom in which this message board resides.
     * @return Room, cannot be null.
     */
    public Room getRoom()
    {
        return room;
    }

    /**
     * Sets the room in which this message board resides.
     * @param room The room. Cannot be null.
     */
    public void setRoom(Room room)
    {
        this.room = room;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Board))
        {
            return false;
        }
        Board other = (Board) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "mmud.database.entities.game.Board[ id=" + id + " ]";
    }

    public boolean addMessage(User aUser, String message)
    {
        BoardMessage boardMessage = new BoardMessage();
        boardMessage.setBoard(this);
        boardMessage.setMessage(message);
        boardMessage.setPerson(aUser);
        boardMessage.setPosttime(new Date());
        boardMessage.setRemoved(false);
        return messages.add(boardMessage);
    }

    @Override
    public String getMainTitle() throws MudException
    {
        return getName();
    }

    @Override
    public String getImage() throws MudException
    {
        // TODO : add an image to the board, we can suffice for now with a generic
        // image, but it would be nice to have an image per board.
        // TODO : add an image field to the board table.
        return null;
    }

    @Override
    public String getBody() throws MudException
    {
        StringBuilder builder = new StringBuilder(getDescription());
        Long now = (new Date()).getTime();
        builder.append("<hr/>");
        for (BoardMessage message : messages)
        {
            if (now - message.getPosttime().getTime() < ONE_WEEK)
            {
                builder.append(message.getPosttime());
                builder.append("<p>");
                builder.append(message.getMessage());
                builder.append("</p><i>");
                builder.append(message.getPerson().getName());
                builder.append("</i>");
                builder.append("<hr/>");
            }
        }
        return builder.toString();
    }
}
