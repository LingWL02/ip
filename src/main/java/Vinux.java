import java.util.Scanner;

public class Vinux {
    public static void main(String[] args) {
        //logo of the chatbox: VINUX
        String logo = "              ________   __\n"
                + "   |\\      /| \\__  __/  ( (    /| |\\      /| |\\      /|\n"
                + "   | )    ( |    ) (    |  \\  ( | | )    ( | ( \\    / )\n"
                + "   | |    | |    | |    |   \\ | | | |    | |  \\ (__) /\n"
                + "   ( (    ) )    | |    | (\\ \\) | | |    | |   ) __ (\n"
                + "    \\ \\__/ /     | |    | | \\   | | |    | |  / (  ) \\\n"
                + "     \\    /   ___) (___ | )  \\  | | (____) | ( /    \\ )\n"
                + "      \\__/    \\_______/ |/    )_) (________) |/      \\|\n";

        //print welcome message to user
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println(logo);
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("Hello! I am your favourite assistant Vinux.");
        System.out.println("I'm listening, unfortunately. Go on.");
        System.out.println("    ____________________________________________________________");

        //Level-2: create array to store tasks (max 100 tasks)
        String[] tasks = new String[100];
        //Level-3: create array to track if tasks are done (true = done, false = not done)
        boolean[] isDone = new boolean[100];
        //Level-4: create array to track task type: T for ToDos, D for Deadlines, E for Events
        String[] taskType = new String[100];
        int taskCount = 0; //counter to keep track of how many tasks are stored

        //initialize scanner to read user input from the console
        Scanner scanner = new Scanner(System.in);
        String input = ""; //user input

        //continuously read and echo user input until "bye" is entered
        while (!input.equals("bye")) {
            input = scanner.nextLine(); //read input as long as condition is true

            //process the input unless the user typed "bye"
            if (!input.equals("bye")) {
                System.out.println("    ____________________________________________________________");

                //display all the tasks with their status and type
                if (input.equals("list")) {
                    //display all stored tasks
                    System.out.println("    Why do you have so many things to do?");
                    System.out.println("    These are your tasks:");
                    for (int i = 0; i < taskCount; i++) {
                        //Level-3: display [X] if done, [] if not done
                        String status = isDone[i] ? "[X]" : "[ ]";
                        //display task number, type, status and description
                        System.out.println("    " + (i + 1) + ".[" + taskType[i] + "]" + status + " " + tasks[i]);
                    }
                } else if (input.startsWith("mark ")) { //Level-3: mark a task as done
                    //Level-5: Error handling for mark command
                    try {
                        //parseInt --> convert string (e.g. "2" to 2)
                        //gives everything from position 5  (e.g. "mark 2" --> 2 is at position 5)
                        int taskNumber = Integer.parseInt(input.substring(5)) - 1; //get task number

                        //check if task number is valid
                        if (taskNumber < 0 || taskNumber >= taskCount) {
                            System.out.println("    Sleepy, much? Task number " + (taskNumber + 1) + " doesn't exist!");
                            System.out.println("    You only have " + taskCount + " task(s) in the list.");
                        } else { //normal case--> whne there are no errors
                            isDone[taskNumber] = true; //set condition as true
                            System.out.println("    Solid! This task is now done (FINALLY!):");
                            System.out.println("        [X] " + tasks[taskNumber]);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("    Excuse me? Please provide a valid task number.");
                        System.out.println("    Try this: mark 1");
                    } catch (StringIndexOutOfBoundsException e) {
                        System.out.println("    Excuse me? Tell me which task you want to mark clearly.");
                        System.out.println("    Try this: mark 1");
                    }
                } else if (input.startsWith("unmark ")) { //Level-3: mark a task as undone
                    //Level-5: Error handling for unmark command
                    try {
                        //parseInt --> convert string (e.g. "2" to 2)
                        //gives everything from position 7 (e.g. "unmark 2" --> 2 is at position 5)
                        int taskNumber = Integer.parseInt(input.substring(7)) - 1;

                        //check if task number is valid
                        if (taskNumber < 0 || taskNumber >= taskCount) {
                            System.out.println("    Sleepy, much? Task number " + (taskNumber + 1) + " doesn't exist!");
                            System.out.println("    You only have " + taskCount + " task(s) in the list.");
                        } else {
                            isDone[taskNumber] = false; //set condition as false
                            System.out.println("    Aw man! This task is still not done:");
                            System.out.println("        [ ] " + tasks[taskNumber]);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("    Excuse me? Please provide a valid task number.");
                        System.out.println("    Try this: unmark 1");
                    } catch (StringIndexOutOfBoundsException e) {
                        System.out.println("    Excuse me? Tell me which task you want to mark clearly.");
                        System.out.println("    Try this: unmark 1");
                    }
                } else if (input.startsWith("delete ")) { //Level-6: delete a task
                    //error handling for delete command
                    try {
                        //parseInt --> convert string to integer
                        //gets everything from position 7 (e.g. "delete 3" --> 3 is at position 7)
                        int taskNumber = Integer.parseInt(input.substring(7)) - 1; //get task number

                        //check if task number is valid
                        if (taskNumber < 0 || taskNumber >= taskCount) {
                            System.out.println("    Sleepy, much? Task number " + (taskNumber + 1) + " doesn't exist!");
                            System.out.println("    You only have " + taskCount + " task(s) in the list.");
                        } else {
                            //store the deleted task info to display it
                            String deletedTask = tasks[taskNumber];
                            String deletedType = taskType[taskNumber];
                            boolean deletedStatus = isDone[taskNumber];
                            String status = deletedStatus ? "[X]" : "[ ]";

                            //shift all tasks after the deleted one, one position to the left
                            for (int i = taskNumber; i < taskCount - 1; i++) {
                                tasks[i] = tasks[i + 1];
                                isDone[i] = isDone[i + 1];
                                taskType[i] = taskType[i + 1];
                            }
                            //decrease task count
                            taskCount--;

                            //display confirmation message
                            System.out.println("    You sure? I've removed this task:");
                            System.out.println("    [" + deletedType + "]" + status + " " + deletedTask);
                            System.out.println("    Now you have " + taskCount + " task(s) in the list.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("    Excuse me? Please provide a valid task number.");
                        System.out.println("    Try this: delete 1");
                    } catch (StringIndexOutOfBoundsException e) {
                        System.out.println("    Excuse me? Tell me which task you want to delete clearly.");
                        System.out.println("    Try this: delete 1");
                    }
                } else if (input.startsWith("todo")) { //Level-4: check if ToDo task (no date/time)
                    //Level-5: error handling for todo command
                    if (input.trim().equals("todo") || input.substring(4).trim().isEmpty()) {
                        //empty todo description
                        System.out.println("    Wake up! You are giving me an empty task?");
                        System.out.println("    Try this: todo buy apples");
                    } else {
                        String description = input.substring(5); //everything after "todo"
                        tasks[taskCount] = description;
                        taskType[taskCount] = "T";
                        isDone[taskCount] = false; //task is undone yet
                        taskCount++;
                        System.out.println("    Gotcha. I have now added this task:");
                        System.out.println("        [T][ ] " + description);
                        System.out.println("    Now you have " + taskCount + " task(s) in the list.");
                    }
                } else if (input.startsWith("deadline")) { //Level-4: check if deadline task
                    //Level-5: error handling for deadline command
                    if (input.trim().equals("deadline") || input.substring(8).trim().isEmpty()) {
                        //empty deadline description
                        System.out.println("    Wake up! When is the deadline??");
                        System.out.println("    Try: deadline return book /by Sunday");
                    } else {
                        String details = input.substring(9); //everything after "deadline"

                        //check if /by is present
                        if (!details.contains(" /by ")) {
                            System.out.println("    Uhm, I need to know the deadline.");
                            System.out.println("    Format: deadline <task> /by <date>");
                        } else {
                            try {
                                //split the input by "/by" to separate descrption and deadline
                                String[] parts = details.split(" /by "); //get the task

                                if (parts[0].trim().isEmpty()) {
                                    //if the description field is empty
                                    System.out.println("    Excuse me? What task are you talking about?");
                                } else if (parts.length < 2 || parts[1].trim().isEmpty()) {
                                    //if the deadline field is empty
                                    System.out.println("    Excuse me? When is the deadline?");
                                } else {
                                    String description = parts[0]; //get task name
                                    String by = parts[1]; //get deadline
                                    tasks[taskCount] = description + " (by: " + by + ")";
                                    taskType[taskCount] = "D";
                                    isDone[taskCount] = false;
                                    taskCount ++;
                                    System.out.println("    Gotcha. I have now added this task:");
                                    System.out.println("        [D][ ] " + description + " (by: " + by + ")");
                                    System.out.println("    Now you have " + taskCount + " task(s) in the list.");
                                }
                            } catch (Exception e) {
                                System.out.println("    Wait...something went wrong with the deadline...");
                                System.out.println("    Try: deadline return book /by Sunday");
                            }
                        }
                    }
                } else if (input.startsWith("event")) { //Level-4: check if event task
                    //Level-5: error handling for event command
                    if (input.trim().equals("event") || input.substring(5).trim().isEmpty()) {
                        //empty event description
                        System.out.println("    Wake up! What is the event even?");
                        System.out.println("    Try: event meeting /from Mon 2pm /to 4pm");
                    } else {
                        String details = input.substring(6); //everything after "event"

                        //check if /from and /to are present
                        if (!details.contains(" /from ")) {
                            System.out.println("    Excuse me? When does the event start?");
                            System.out.println("    Format: event <task> /from <start> /to <end>");
                        } else if (!details.contains(" /to ")) {
                            System.out.println("    Excuse me? When does the event end?");
                            System.out.println("    Format: event <task> /from <start> /to <end>");
                        } else {
                            try {
                                //split input by "/from" and "/to" to extract start and end time
                                int fromIndex = details.indexOf(" /from "); //get the from time
                                int toIndex = details.indexOf(" /to "); //get the to time

                                if (fromIndex >= toIndex) {
                                    System.out.println("    Uhm...the /to must come after /from!");
                                } else {
                                    String description = details.substring(0, fromIndex); //get task name
                                    String from = details.substring(fromIndex + 7, toIndex); //get 'from' date
                                    String to = details.substring(toIndex + 5); //get 'to' date

                                    if (description.trim().isEmpty()) {
                                        System.out.println("    Wake up! What is the event even?");
                                    } else if (from.trim().isEmpty()) {
                                        System.out.println("    Excuse me? When does the event start?");
                                    } else if (to.trim().isEmpty()) {
                                        System.out.println("    Excuse me? When does the event end?");
                                    } else {
                                        tasks[taskCount] = description + " (from: " + from + " to: " + to + ")";
                                        taskType[taskCount] = "E";
                                        isDone[taskCount] = false;
                                        taskCount ++;
                                        System.out.println("    Gotcha. I have now added this task:");
                                        System.out.println("    [E][ ] " + description + " (from: " + from + " to: " + to + ")");
                                        System.out.println("    Now you have " + taskCount + " task(s) in the list.");
                                    }
                                }
                            } catch (Exception e) {
                                System.out.println("    Wait...something went wrong with the event...");
                                System.out.println("    Try: event meeting /from Mon 2pm /to 4pm");
                            }
                        }
                    }
                } else {
                    //Level-5: Unknown command error
                    System.out.println("    OOPS!!! I'm sorry, but I don't know what that means :-(");
                    System.out.println("    Try: todo, deadline, event, list, mark, or unmark");
                }

                System.out.println("    ____________________________________________________________");
            }
        }

        //print goodbye message and exit (from Level-0)
        System.out.println("Bye. Try not to miss me too much ;)");
        System.out.println("||~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~||");
        System.out.println("||~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~||");

        scanner.close(); //always close the scanner
    }
}


