package ClientDocker.Query;



public class ShortReadQuery7 implements Runnable {
    private int commentId;
    private String content;
    private String creationDate;


    public ShortReadQuery7(int commentId, String content, String creationDate) {
        this.commentId = commentId;
        this.content = content;
        this.creationDate = creationDate;
    }

    public void run() {
        try {
            // Step 1:
            // Find the comment according to messageId
            // check if commentId equals Message.id




            // Step 2:
            // Link the comment to person (replyAuthor)
            // check if commentId equals Person.id




            // Step 3:
            // check if messageAuthor equals Person's name (FirseName + LastName)
            // return a boolean variable knows = true if the messageAuthor is the same as the Person's name

        } catch (Exception e) {
            e.printStackTrace();
        } 
        System.out.println("ShortReadQuery7 finished");
    }
}
