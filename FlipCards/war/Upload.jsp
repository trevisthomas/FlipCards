<%@ page import="com.google.appengine.api.blobstore.BlobstoreService" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreServiceFactory" %>
<body>
	<%BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService(); %>
    <form action="<%= blobstoreService.createUploadUrl("/flipcards/upload") %>" method="post" enctype="multipart/form-data">
        <input type="file" name="myFile">
        <input type="submit" value="Submit">
    </form>
</body>