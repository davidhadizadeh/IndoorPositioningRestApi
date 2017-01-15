package de.hadizadeh.positioning.api.rest;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import de.hadizadeh.positioning.roommodel.FileManager;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Rest resource
 */
@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MainResource {
    private static final String ROOM_MODEL_FILE_NAME = "roomModelData.mef";
    private static final String POSITIONING_FILE_NAME = "positioningPersistence.xml";

    /**
     * Returns all available project names
     *
     * @return http response with project names
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProjectNames() {
        List<String> projects = new ArrayList<String>();
        File projectDir = new File(getDataDir());
        for (File project : projectDir.listFiles()) {
            projects.add(project.getName());
        }
        return Response.ok(projects).build();
    }

    /**
     * Checks if the transferred mef file hash is the same as he existing mef file
     *
     * @param positioningProject positioning project
     * @param mefHash            mef hash
     * @return http response, status accept if there are new data, or status no content, if the data are already the same
     */
    @GET
    @Produces("application/mef")
    @Path("/{positioningProject}/mef/check/{mefHash}")
    public Response getMefFileInfo(@PathParam("positioningProject") String positioningProject, @PathParam("mefHash") String mefHash) {
        Response fileResponse = getFile(positioningProject, mefHash, ROOM_MODEL_FILE_NAME);
        if (fileResponse.getStatus() == Response.Status.OK.getStatusCode()) {
            return Response.status(Response.Status.ACCEPTED).build();
        } else {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
    }

    /**
     * Returns the mef file
     *
     * @param positioningProject positioning project
     * @param mefHash            mef hash
     * @return mef file or no content status, if the file is the same as the transferred hash
     */
    @GET
    @Produces("application/mef")
    @Path("/{positioningProject}/mef/{mefHash}")
    public Response getMefFile(@PathParam("positioningProject") String positioningProject, @PathParam("mefHash") String mefHash) {
        return getFile(positioningProject, mefHash, ROOM_MODEL_FILE_NAME);
    }

    /**
     * Returns the positioning file
     *
     * @param positioningProject positioning project
     * @param hash               positioning file hash
     * @return positioning file or no content status, if the file is the same as the transferred hash
     */
    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/{positioningProject}/positioning/{hash}")
    public Response getPositioningFile(@PathParam("positioningProject") String positioningProject, @PathParam("hash") String hash) {
        return getFile(positioningProject, hash, POSITIONING_FILE_NAME);
    }

    /**
     * Removes the positioning file
     *
     * @param positioningProject positioning project
     * @return ok if the file has been removed, else not found
     */
    @DELETE
    @Produces(MediaType.APPLICATION_XML)
    @Path("/{positioningProject}")
    public Response getPositioningFile(@PathParam("positioningProject") String positioningProject) {
        File file = new File(getDataDir() + positioningProject);
        if (file.isDirectory()) {
            FileManager.removeDirectory(file);
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    private Response getFile(String positioningProject, String currentHash, String fileName) {
        File file = new File(getDataDir() + positioningProject + File.separator + fileName);
        if (file.exists()) {
            String hash = FileManager.calculateHash(file);
            if (!currentHash.equals(hash)) {
                Response.ResponseBuilder response = Response.ok(file);
                response.header("Content-Disposition", "attachment; filename=" + fileName);
                return response.build();
            } else {
                return Response.status(Response.Status.NO_CONTENT).build();
            }
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    /**
     * Creates a remote project
     *
     * @param positioningProject positioning project name
     * @return created, if the project has been created, else http conflict
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/{positioningProject}")
    public Response createProject(@PathParam("positioningProject") String positioningProject) {
        File path = new File(getDataDir() + positioningProject);
        if (!path.exists()) {
            path.mkdirs();
            return Response.status(Response.Status.CREATED).build();
        } else {
            return Response.status(Response.Status.CONFLICT).build();
        }
    }

    /**
     * Updates the mef file
     *
     * @param positioningProject       positioning project
     * @param fileInputStream          uploaded mef file
     * @param contentDispositionHeader header of the file
     * @return create, if success, else it failed
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/{positioningProject}/mef")
    public Response updateMefFile(@PathParam("positioningProject") String positioningProject,
                                  @FormDataParam("file") InputStream fileInputStream,
                                  @FormDataParam("file") FormDataContentDisposition contentDispositionHeader) {
        return updateFile(positioningProject, fileInputStream, contentDispositionHeader, ROOM_MODEL_FILE_NAME, "mef");
    }

    /**
     * Updates the positioning file
     *
     * @param positioningProject       positioning project
     * @param fileInputStream          uploaded positioning file
     * @param contentDispositionHeader header of the file
     * @return create, if success, else it failed
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/{positioningProject}/positioning")
    public Response updatePositioningFile(@PathParam("positioningProject") String positioningProject,
                                          @FormDataParam("file") InputStream fileInputStream,
                                          @FormDataParam("file") FormDataContentDisposition contentDispositionHeader) {
        return updateFile(positioningProject, fileInputStream, contentDispositionHeader, POSITIONING_FILE_NAME, "xml");
    }

    private Response updateFile(String positioningProject, InputStream fileInputStream, FormDataContentDisposition contentDispositionHeader, String fileName, String extension) {
        String fileExtension = "";
        int i = contentDispositionHeader.getFileName().lastIndexOf('.');
        if (i > 0) {
            fileExtension = contentDispositionHeader.getFileName().substring(i + 1);
        }
        if (extension.equals(fileExtension)) {
            String projectPath = getDataDir() + positioningProject + File.separator;
            new File(projectPath).mkdirs();
            if (writeToFile(fileInputStream, projectPath + fileName)) {
                return Response.status(Response.Status.CREATED).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    private boolean writeToFile(InputStream uploadedInputStream, String uploadedFileLocation) {
        try {
            int read = 0;
            byte[] bytes = new byte[1024];
            OutputStream out = new FileOutputStream(new File(uploadedFileLocation));
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String getDataDir() {
        Properties properties = new Properties();
        try {
            properties.load(MainResource.class.getResourceAsStream("config.properties"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (String) properties.get("data_dir");
    }
}
