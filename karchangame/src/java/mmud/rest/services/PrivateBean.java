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
package mmud.rest.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import mmud.database.entities.game.*;
import mmud.database.entities.characters.*;
import mmud.database.entities.web.CharacterInfo;
import mmud.database.entities.web.Family;
import mmud.database.entities.web.FamilyPK;
import mmud.database.entities.web.FamilyValue;
import mmud.rest.webentities.PrivateMail;
import mmud.rest.webentities.PrivatePerson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains all rest calls that are available to a specific character, with
 * authentication and authorization. You can find them at
 * /karchangame/resources/private.
 *
 * @author maartenl
 */
@Stateless
@LocalBean
@Path("/private")
public class PrivateBean
{

    @EJB
    private MailBean mailBean;
    /**
     * Indicates that only 20 mails may be retrieved per time (basically a
     * page).
     */
    public static final int MAX_MAILS = 20;
    @PersistenceContext(unitName = "karchangamePU")
    private EntityManager em;

    /**
     * Returns the entity manager of Hibernate/JPA. This is defined in
     * build/web/WEB-INF/classes/META-INF/persistence.xml.
     *
     * @return EntityManager
     */
    protected EntityManager getEntityManager()
    {
        return em;
    }
    private static final Logger itsLog = LoggerFactory.getLogger(PrivateBean.class);

    /**
     * This method should be called to verify that the target of a certain
     * action is indeed a proper authenticated user.
     *
     * @param lok session password
     * @param name the name to identify the person
     * @throws WebApplicationException NOT_FOUND, if the user is either not
     * found or is not a proper user. BAD_REQUEST if an unexpected exception
     * crops up or provided info is really not proper. UNAUTHORIZED if session
     * passwords do not match.
     */
    private User authenticate(String name, String lok)
    {
        User person = getEntityManager().find(User.class, name);
        if (person == null)
        {
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        if (!person.isUser())
        {
            throw new WebApplicationException(Status.BAD_REQUEST);
        }
        if (!person.verifySessionPassword(lok))
        {
            throw new WebApplicationException(Status.UNAUTHORIZED);
        }
        return person;
    }

    /**
     * Returns a List of your mail, with a maximum of 20 mails and an offset for
     * paging.
     *
     * @param lok the hash to use for verification of the user, is the lok
     * setting in the cookie when logged onto the game.
     * @param name the name of the user
     * @param offset the offset, default is 0 if not provided.
     * @throws WebApplicationException <ul><li>UNAUTHORIZED, if the
     * authorisation failed. </li><li>BAD_REQUEST if an unexpected exception
     * crops up.</li></ul>
     */
    @GET
    @Path("{name}/mail")
    @Produces(
    {
        MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON
    })
    public List<PrivateMail> listMail(@PathParam("name") String name, @QueryParam("offset") Integer offset, @QueryParam("lok") String lok)
    {
        itsLog.debug("entering listMail");
        Person person = authenticate(name, lok);
        List<PrivateMail> res = new ArrayList<>();
        try
        {
            Query query = getEntityManager().createNamedQuery("Mail.listmail");
            query.setParameter("name", person);
            query.setMaxResults(MAX_MAILS);
            if (offset != null)
            {
                query.setFirstResult(offset);
            }

            List<Mail> list = query.getResultList();
            for (Mail mail : list)
            {
                PrivateMail pmail = new PrivateMail(mail);
                res.add(pmail);
            }

            // turn off the "newmail" sign.
            query = getEntityManager().createNamedQuery("Mail.nonewmail");
            query.setParameter("name", person);
            query.executeUpdate();
        } catch (WebApplicationException e)
        {
            //ignore
            throw e;
        } catch (Exception e)
        {
            itsLog.debug("listMail: throws ", e);
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        }
        return res;
    }

    /**
     * Returns a boolean, indicating if you have new mail. (i.e. mail since you
     * last checked)
     *
     * @param lok the hash to use for verification of the user, is the lok
     * setting in the cookie when logged onto the game.
     * @param name the name of the user
     * @throws WebApplicationException UNAUTHORIZED, if the authorisation
     * failed. BAD_REQUEST if an unexpected exception crops up.
     */
    @GET
    @Path("{name}/newmail")
    @Consumes(
    {
        MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON
    })
    public Response hasNewMail(@PathParam("name") String name, @QueryParam("lok") String lok)
    {
        itsLog.debug("entering hasNewMail");
        Person person = authenticate(name, lok);
        boolean result = mailBean.hasNewMail(person);
        if (!result)
        {
            return Response.noContent().build();
        }
        return Response.ok().build();
    }

    private Person getPerson(String name)
    {
        Person toperson = getEntityManager().find(Person.class, name);
        if (toperson == null)
        {
            itsLog.warn("name of non existing user {}", name);
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return toperson;
    }

    private User getUser(String name)
    {
        User toperson = getEntityManager().find(User.class, name);
        if (toperson == null)
        {
            itsLog.warn("name of non existing user {}", name);
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        if (!toperson.isUser())
        {
            itsLog.warn("user not proper user, {}", name);
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        return toperson;
    }

    /**
     * Compose a new mail.
     *
     * @param lok the hash to use for verification of the user, is the lok
     * setting in the cookie when logged onto the game.
     * @param name the name of the user
     * @throws WebApplicationException UNAUTHORIZED, if the authorisation
     * failed. BAD_REQUEST if an unexpected exception crops up.
     */
    @POST
    @Path("{name}/mail")
    @Consumes(
    {
        MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON
    })
    public Response newMail(PrivateMail newMail, @PathParam("name") String name, @QueryParam("lok") String lok)
    {
        itsLog.debug("entering newMail");
        User person = authenticate(name, lok);
        User toperson = getUser(newMail.toname);
        try
        {
            Mail mail = new Mail();
            mail.setBody(newMail.body);
            mail.setDeleted(Boolean.FALSE);
            mail.setHaveread(Boolean.FALSE);
            mail.setItemDefinition(null);
            mail.setId(null);
            mail.setName(person);
            mail.setNewmail(Boolean.TRUE);
            mail.setSubject(newMail.subject);
            mail.setToname(toperson);
            mail.setWhensent(new Date());
            getEntityManager().persist(mail);
        } catch (WebApplicationException e)
        {
            //ignore
            throw e;
        } catch (Exception e)
        {
            itsLog.debug("newMail: throws ", e);
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        }
        itsLog.debug("exiting newMail");
        return Response.ok().build();
    }

    private Mail getMail(String toname, Long id)
    {
        Mail mail = getEntityManager().find(Mail.class, id);

        if (mail == null)
        {
            itsLog.warn("mail {} not found", id);
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        if (mail.getDeleted())
        {
            itsLog.warn("mail {} deleted", id);
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        if (!mail.getToname().getName().equals(toname))
        {
            itsLog.warn("mail {} not for {}", id, toname);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        return mail;
    }

    /**
     * Returns a single mail based by id. Can only retrieve mail destined for
     * the user requesting the mail.
     *
     * @param lok the hash to use for verification of the user, is the lok
     * setting in the cookie when logged onto the game.
     * @param name the name of the user
     * @param id the id of the mail to get
     * @throws WebApplicationException UNAUTHORIZED, if the authorisation
     * failed. BAD_REQUEST if an unexpected exception crops up.
     */
    @GET
    @Path("{name}/mail/{id}")
    @Produces(
    {
        MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON
    })
    public PrivateMail getMail(@PathParam("name") String name, @QueryParam("lok") String lok, @PathParam("id") long id)
    {
        itsLog.debug("entering getMail");

        Person person = authenticate(name, lok);
        try
        {
            Mail mail = getMail(person.getName(), id);
            // turn off the "have not read" sign.
            mail.setHaveread(Boolean.TRUE);
            itsLog.debug("exiting getMail");
            return new PrivateMail(mail);
        } catch (WebApplicationException e)
        {
            //ignore
            throw e;
        } catch (Exception e)
        {
            itsLog.debug("getMail: throws ", e);
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        }
    }

    /**
     * Creates an itemDefinitionId instance (and, if required, an
     * itemDefinitionId definition) representing an in-game version of a single
     * mail based by id. <img
     * src="../../../images/PrivateBean_createMailItem.png">
     *
     * @param lok the hash to use for verification of the user, is the lok
     * setting in the cookie when logged onto the game.
     * @param name the name of the user
     * @param id the id of the mail to get
     * @param itemDefinitionId the kind of itemDefinitionId that is to be made.
     * It may be null, if there already is attached a item definition to the
     * mail.
     * @see Mail#ITEMS
     * @throws WebApplicationException UNAUTHORIZED, if the authorisation
     * failed. BAD_REQUEST if an unexpected exception crops up.
     * @startuml PrivateBean_createMailItem.png
     * (*) --> "check params"
     * --> "getMail"
     * if "has Item Definition" then
     * ->[true] "create instance object"
     * else
     * ->[false] "get maxid"
     * --> "create itemDefinitionId definition"
     * --> "set itemdefinition into the mail"
     * --> "create instance object"
     * endif
     * --> "create inventory object"
     * -->(*)
     * @enduml
     */
    @GET
    @Path("{name}/mail/{id}/createMailItem/{item}")
    @Consumes(
    {
        MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON
    })
    public Response createMailItem(@PathParam("name") String name, @QueryParam("lok") String lok, @PathParam("id") long id, @PathParam("item") int itemDefinitionId)
    {
        return Response.noContent().build();
        // TODO MLE: this needs to get fixed.
//
//        itsLog.debug("entering createMailItem");
//        Person person = authenticate(name, lok);
//
//        try
//        {
//            // get the specific mail with id {id}
//            Mail mail = getMail(person.getName(), id);
//
//            itsLog.debug("createMailItem: retrieve template item definition");
//
//            ItemDefinition newdef = null;
//            if (mail.getItemDefinition() == null)
//            {
//                if (itemDefinitionId >= Mail.ITEMS.length && itemDefinitionId < 0)
//                {
//                    itsLog.debug("createMailItem: wrong item def");
//                    throw new WebApplicationException(Response.Status.BAD_REQUEST);
//                }
//
//                ItemDefinition definition = getEntityManager().find(ItemDefinition.class, Mail.ITEMS[itemDefinitionId]);
//
//                int max_id = 0;
//                // retrieve max item_idItemDefinition.maxid
//                itsLog.debug("createMailItem: retrieve max id");
//                Query query = getEntityManager().createNamedQuery("ItemDefinition.maxid");
//                max_id = (Integer) query.getSingleResult();
//
//                // create itemDefinitionId definition
//                itsLog.debug("createMailItem: create item definition");
//                newdef = new ItemDefinition();
//                newdef.setId(max_id + 1);
//                newdef.setName(definition.getName());
//                newdef.setAdject1(definition.getAdject1());
//                newdef.setAdject2(definition.getAdject2());
//                newdef.setAdject3(definition.getAdject3());
//                newdef.setGetable(definition.getGetable());
//                newdef.setDropable(definition.getDropable());
//                newdef.setVisible(definition.getVisible());
//                newdef.setDescription(definition.getDescription());
//                newdef.setReaddescription(definition.getReaddescription().replace("letterhead", "<div id=\"karchan_letterhead\">" + mail.getSubject() + "</div>").replace("letterbody", "<div id=\"karchan_letterbody\">" + mail.getBody() + "</div>").replace("letterfooter", "<div id=\"karchan_letterfooter\">" + mail.getName().getName() + "</div>"));
//
//                newdef.setCopper(definition.getCopper());
//                newdef.setOwner(definition.getOwner());
//                newdef.setNotes(definition.getNotes());
//                newdef.setCreation(new Date());
//                getEntityManager().persist(newdef);
//
//                // set itemDefinitionId definition into the mail
//                itsLog.debug("createMailItem: set itemdefinition into the mail");
//                mail.setItemDefinition(newdef);
//            }
//
//            // create itemDefinitionId instance
//            itsLog.debug("createMailItem: create instance object " + mail.getItemDefinition() + " " + name);
//
//            Item item = new Item();
//            item.setOwner(getEntityManager().find(Admin.class, Admin.DEFAULT_OWNER));
//            item.setItemDefinition(mail.getItemDefinition());
//            item.setCreation(new Date());
//            getEntityManager().persist(item);
//
//            if (item.getId() != null)
//            {
//                // put item instance into inventory
//                itsLog.debug("createMailItem: create inventory object for " + name);
//
//                CharitemTable inventory = new CharitemTable();
//                inventory.setId(item.getId());
//                inventory.setBelongsto(person);
//                inventory.setItem(item);
//                getEntityManager().persist(inventory);
//            }
//        } catch (WebApplicationException e)
//        {
//            //ignore
//            throw e;
//        } catch (ConstraintViolationException e)
//        {
//            StringBuilder buffer = new StringBuilder("ConstraintViolationException:");
//            for (ConstraintViolation<?> violation : e.getConstraintViolations())
//            {
//                buffer.append(violation);
//            }
//            throw new RuntimeException(buffer.toString(), e);
//
//        } catch (Exception e)
//        {
//            itsLog.debug("createMailItem: throws ", e);
//            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
//        }
//        itsLog.debug("exiting createMailItem");
//        return Response.ok().build();
    }

    /**
     * Deletes a single mail based by id.
     *
     * @param lok the hash to use for verification of the user, is the lok
     * setting in the cookie when logged onto the game.
     * @param name the name of the user
     * @param id the id of the mail to delete
     * @throws WebApplicationException UNAUTHORIZED, if the authorisation
     * failed. BAD_REQUEST if an unexpected exception crops up.
     */
    @DELETE
    @Path("{name}/mail/{id}")
    @Consumes(
    {
        MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON
    })
    public Response deleteMail(@PathParam("name") String name, @QueryParam("lok") String lok, @PathParam("id") long id)
    {
        itsLog.debug("entering deleteMail");
        Person person = authenticate(name, lok);
        try
        {
            // get the specific mail with id {id}
            Mail mail = getMail(person.getName(), id);
            mail.setDeleted(Boolean.TRUE);

        } catch (WebApplicationException e)
        {
            //ignore
            throw e;
        } catch (Exception e)
        {
            itsLog.debug("deleteMail: throws ", e);
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        }
        itsLog.debug("exiting deleteMail");
        return Response.ok().build();
    }

    /**
     * Adds or updates your current character info.
     *
     * @param name the name of the user
     * @param lok the dession password
     * @param cinfo the object containing the new stuff to update.
     * @throws WebApplicationException UNAUTHORIZED, if the authorisation
     * failed. BAD_REQUEST if an unexpected exception crops up.
     */
    @PUT
    @Path("{name}/charactersheet")
    @Consumes(
    {
        MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON
    })
    public Response updateCharacterSheet(@PathParam("name") String name, @QueryParam("lok") String lok, PrivatePerson cinfo)
    {
        itsLog.debug("entering updateCharacterSheet");
        Person person = authenticate(name, lok);
        if (!person.getName().equals(cinfo.name))
        {
            itsLog.debug("updateCharacterSheet: names not the same {} and {}", person.getName(), cinfo.name);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        try
        {
            CharacterInfo characterInfo = getEntityManager().find(CharacterInfo.class, person.getName());
            boolean isNew = false;
            if (characterInfo == null)
            {
                isNew = true;
                characterInfo = new CharacterInfo();
                characterInfo.setName(person.getName());
            }
            characterInfo.setImageurl(cinfo.imageurl);
            characterInfo.setHomepageurl(cinfo.homepageurl);
            characterInfo.setDateofbirth(cinfo.dateofbirth);
            characterInfo.setCityofbirth(cinfo.cityofbirth);
            characterInfo.setStoryline(cinfo.storyline);
            if (isNew)
            {
                getEntityManager().persist(characterInfo);
            }

        } catch (WebApplicationException e)
        {
            //ignore
            throw e;
        } catch (Exception e)
        {
            itsLog.debug("updateCharacterSheet: throws ", e);
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        }
        return Response.ok().build();
    }

    /**
     * Add or updates some family values from your family tree.
     *
     * @param lok the hash to use for verification of the user, is the lok
     * setting in the cookie when logged onto the game.
     * @param name the name of the user
     * @param toname the name of the user you are related to
     * @param description the description of the family relation, for example
     * "mother".
     * @throws WebApplicationException UNAUTHORIZED, if the authorisation
     * failed. BAD_REQUEST if an unexpected exception crops up.
     */
    @PUT
    @Path("{name}/charactersheet/familyvalues/{toname}/{description}")
    @Consumes(
    {
        MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON
    })
    public Response updateFamilyvalues(@PathParam("name") String name, @QueryParam("lok") String lok, @PathParam("toname") String toname, @PathParam("description") Integer description)
    {
        itsLog.debug("entering updateFamilyvalues");
        if (description == null || description == 0 || toname == null || "".equals(toname.trim()))
        {
            itsLog.error("updateFamilyValues bad params");
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        User person = authenticate(name, lok);
        Person toperson = getPerson(toname);
        try
        {
            FamilyValue familyValue = getEntityManager().find(FamilyValue.class, description);
            if (familyValue == null)
            {
                itsLog.error("updateFamilyValues family value not found");
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }
            FamilyPK pk = new FamilyPK();
            pk.setName(person.getName());
            pk.setToname(toperson.getName());
            Family family = getEntityManager().find(Family.class, pk);

            boolean isNew = family == null;
            if (isNew)
            {
                family = new Family();
                family.setFamilyPK(pk);
            }
            family.setDescription(familyValue);
            if (isNew)
            {
                getEntityManager().persist(family);
            }
        } catch (WebApplicationException e)
        {
            //ignore
            throw e;
        } catch (Exception e)
        {
            itsLog.debug("updateFamilyvalues: throws ", e);
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        }
        itsLog.debug("exiting updateFamilyvalues");
        return Response.ok().build();
    }

    /**
     * Deletes some family values from your family tree.
     *
     * @param lok the hash to use for verification of the user, is the lok
     * setting in the cookie when logged onto the game.
     * @param name the name of the user
     * @param toname the name of the user you are related to
     * @throws WebApplicationException UNAUTHORIZED, if the authorisation
     * failed. BAD_REQUEST if an unexpected exception crops up.
     */
    @DELETE
    @Path("{name}/charactersheet/familyvalues/{toname}")
    @Consumes(
    {
        MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON
    })
    public Response deleteFamilyvalues(@PathParam("name") String name, @QueryParam("lok") String lok, @PathParam("toname") String toname)
    {
        itsLog.debug("entering deleteFamilyValues");
        Person person = authenticate(name, lok);
        try
        {
            FamilyPK pk = new FamilyPK();
            pk.setName(person.getName());
            pk.setToname(toname);
            Family family = getEntityManager().find(Family.class, pk);
            getEntityManager().remove(family);
        } catch (Exception e)
        {
            itsLog.debug("deleteFamilyValues: throws", e);
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        }
        itsLog.debug("exiting deleteFamilyValues");
        return Response.ok().build();
    }
}
