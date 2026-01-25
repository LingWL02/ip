public class Duke {
    public static void main(String[] args) {
        String logo = " ____        _        \n"
                + "|  _ \\ _   _| | _____ \n"
                + "| | | | | | | |/ / _ \\\n"
                + "| |_| | |_| |   <  __/\n"
                + "|____/ \\__,_|_|\\_\\___|\n";
        System.out.println("Hello from\n" + logo);
        String separator = "_".repeat(50);

        String introduction = (
                separator +
                "\nHello! I'm Duke! What can I do for you?\n"
                + separator +
                "\nBye. Hope to see you again soon!\n" +
                separator
        );
        System.out.println(introduction);

    }
}
