package org.rouif.notes.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cmd.Query;

import org.rouif.notes.backend.model.Note;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * WARNING: This generated code is intended as a sample or starting point for using a
 * Google Cloud Endpoints RESTful API with an Objectify entity. It provides no data access
 * restrictions and no data validation.
 * <p/>
 * DO NOT deploy this code unchanged as part of a real application to real users.
 */
@Api(
        name = "noteApi",
        version = "v1",
        resource = "note",
        namespace = @ApiNamespace(
                ownerDomain = "model.backend.notes.rouif.org",
                ownerName = "model.backend.notes.rouif.org",
                packagePath = ""
        )
)
public class NoteEndpoint {

    private static final Logger logger = Logger.getLogger(NoteEndpoint.class.getName());

    private static final int DEFAULT_LIST_LIMIT = 20;

    static {
        // Typically you would register this inside an OfyServive wrapper. See: https://code.google.com/p/objectify-appengine/wiki/BestPractices
        ObjectifyService.register(Note.class);
    }

    /**
     * Returns the {@link Note} with the corresponding ID.
     *
     * @param id the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code Note} with the provided ID.
     */
    @ApiMethod(
            name = "get",
            path = "note/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Note get(@Named("id") Long id) throws NotFoundException {
        logger.info("Getting Note with ID: " + id);
        Note note = ofy().load().type(Note.class).id(id).now();
        if (note == null) {
            throw new NotFoundException("Could not find Note with ID: " + id);
        }
        return note;
    }

    /**
     * Inserts a new {@code Note}.
     */
    @ApiMethod(
            name = "insert",
            path = "note",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Note insert(Note note) {
        // Typically in a RESTful API a POST does not have a known ID (assuming the ID is used in the resource path).
        // You should validate that note.id has not been set. If the ID type is not supported by the
        // Objectify ID generator, e.g. long or String, then you should generate the unique ID yourself prior to saving.
        //
        // If your client provides the ID then you should probably use PUT instead.
        ofy().save().entity(note).now();
        logger.info("Created Note with ID: " + note.getId());

        return ofy().load().entity(note).now();
    }

    /**
     * Updates an existing {@code Note}.
     *
     * @param id   the ID of the entity to be updated
     * @param note the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Note}
     */
    @ApiMethod(
            name = "update",
            path = "note/{id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public Note update(@Named("id") Long id, Note note) throws NotFoundException {
        // TODO: You should validate your ID parameter against your resource's ID here.
        checkExists(id);
        ofy().save().entity(note).now();
        logger.info("Updated Note: " + note);
        return ofy().load().entity(note).now();
    }

    /**
     * Deletes the specified {@code Note}.
     *
     * @param id the ID of the entity to delete
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Note}
     */
    @ApiMethod(
            name = "remove",
            path = "note/{id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("id") Long id) throws NotFoundException {
        checkExists(id);
        ofy().delete().type(Note.class).id(id).now();
        logger.info("Deleted Note with ID: " + id);
    }

    /**
     * List all entities.
     *
     * @param cursor used for pagination to determine which page to return
     * @param limit  the maximum number of entries to return
     * @return a response that encapsulates the result list and the next page token/cursor
     */
    @ApiMethod(
            name = "list",
            path = "note",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Note> list(@Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<Note> query = ofy().load().type(Note.class).limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<Note> queryIterator = query.iterator();
        List<Note> noteList = new ArrayList<Note>(limit);
        while (queryIterator.hasNext()) {
            noteList.add(queryIterator.next());
        }
        return CollectionResponse.<Note>builder().setItems(noteList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }

    private void checkExists(Long id) throws NotFoundException {
        try {
            ofy().load().type(Note.class).id(id).safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find Note with ID: " + id);
        }
    }
}