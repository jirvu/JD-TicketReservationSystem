// Source code is decompiled from a .class file using FernFlower decompiler.
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class TicketRecords {
   private final Connection connection;
   private final Scanner scanner;

   public TicketRecords(Connection var1) {
      this.connection = var1;
      this.scanner = new Scanner(System.in);
   }

   public void showMenu() throws SQLException {
      boolean var1 = true;

      while(var1) {
         System.out.println("\nTicket Record Management");
         System.out.println("[1] Add Ticket");
         System.out.println("[2] Remove Ticket");
         System.out.println("[3] View Ticket");
         System.out.println("[4] View Ticket Statistics");
         System.out.println("[5] Exit");
         System.out.print("Choice: ");
         int var2 = this.scanner.nextInt();
         this.scanner.nextLine();
         switch (var2) {
            case 1 -> this.addTicket();
            case 2 -> this.removeTicket();
            case 3 -> this.viewTicket();
            case 4 -> this.viewTicketStatistics();
            case 5 -> {
               System.out.println("Exiting Ticket Records Management...");
               var1 = false;
            }
            default -> System.out.println("Invalid choice. Please try again.");
         }
      }

   }

   private void addTicket() {
      try {
         System.out.print("Ticket ID: ");
         int var1 = this.scanner.nextInt();
         this.scanner.nextLine();
         String var2 = "SELECT COUNT(*) FROM Tickets WHERE ticket_id = ?";
         PreparedStatement var3 = this.connection.prepareStatement(var2);

         label111: {
            try {
               var3.setInt(1, var1);
               ResultSet var4 = var3.executeQuery();

               label113: {
                  try {
                     var4.next();
                     if (var4.getInt(1) <= 0) {
                        break label113;
                     }

                     System.out.println("Ticket ID already exists.");
                  } catch (Throwable var16) {
                     if (var4 != null) {
                        try {
                           var4.close();
                        } catch (Throwable var14) {
                           var16.addSuppressed(var14);
                        }
                     }

                     throw var16;
                  }

                  if (var4 != null) {
                     var4.close();
                  }
                  break label111;
               }

               if (var4 != null) {
                  var4.close();
               }
            } catch (Throwable var17) {
               if (var3 != null) {
                  try {
                     var3.close();
                  } catch (Throwable var13) {
                     var17.addSuppressed(var13);
                  }
               }

               throw var17;
            }

            if (var3 != null) {
               var3.close();
            }

            System.out.print("Event ID: ");
            int var19 = this.scanner.nextInt();
            this.scanner.nextLine();
            System.out.print("Ticket Type: ");
            String var20 = this.scanner.nextLine();
            System.out.print("Price: ");
            BigDecimal var5 = this.scanner.nextBigDecimal();
            this.scanner.nextLine();
            System.out.print("Seat Number: ");
            String var6 = this.scanner.nextLine();

            while(true) {
               System.out.print("Status (Available, Sold, Cancelled): ");
               String var7 = this.scanner.nextLine().trim();
               if (var7.equalsIgnoreCase("Available") || var7.equalsIgnoreCase("Sold") || var7.equalsIgnoreCase("Cancelled")) {
                  String var10000 = var7.substring(0, 1).toUpperCase();
                  var7 = var10000 + var7.substring(1).toLowerCase();
                  String var8 = "INSERT INTO Tickets (ticket_id, event_id, ticket_type, price, seat_number, ticket_status) VALUES (?, ?, ?, ?, ?, ?)";
                  PreparedStatement var9 = this.connection.prepareStatement(var8);

                  try {
                     var9.setInt(1, var1);
                     var9.setInt(2, var19);
                     var9.setString(3, var20);
                     var9.setBigDecimal(4, var5);
                     var9.setString(5, var6);
                     var9.setString(6, var7);
                     int var10 = var9.executeUpdate();
                     System.out.println("" + var10 + " Ticket added successfully.");
                     this.updateTicketStatistics(var19, var20, "add", var7);
                  } catch (Throwable var15) {
                     if (var9 != null) {
                        try {
                           var9.close();
                        } catch (Throwable var12) {
                           var15.addSuppressed(var12);
                        }
                     }

                     throw var15;
                  }

                  if (var9 != null) {
                     var9.close();
                  }

                  return;
               }

               System.out.println("Invalid status.");
            }
         }

         if (var3 != null) {
            var3.close();
         }

      } catch (SQLException var18) {
         System.err.println("Error adding ticket: " + var18.getMessage());
      }
   }

   private void removeTicket() {
      System.out.print("Enter Ticket ID to remove: ");
      int var1 = this.scanner.nextInt();
      this.scanner.nextLine();
      String var2 = "SELECT event_id, ticket_type, ticket_status FROM Tickets WHERE ticket_id = ?";

      int var3;
      String var4;
      String var5;
      try {
         label109: {
            PreparedStatement var6;
            label110: {
               var6 = this.connection.prepareStatement(var2);

               try {
                  label111: {
                     var6.setInt(1, var1);
                     ResultSet var7 = var6.executeQuery();

                     label98: {
                        try {
                           if (!var7.next()) {
                              System.out.println("No ticket found.");
                              break label98;
                           }

                           var3 = var7.getInt("event_id");
                           var4 = var7.getString("ticket_type");
                           var5 = var7.getString("ticket_status");
                        } catch (Throwable var15) {
                           if (var7 != null) {
                              try {
                                 var7.close();
                              } catch (Throwable var11) {
                                 var15.addSuppressed(var11);
                              }
                           }

                           throw var15;
                        }

                        if (var7 != null) {
                           var7.close();
                        }
                        break label111;
                     }

                     if (var7 != null) {
                        var7.close();
                     }
                     break label110;
                  }
               } catch (Throwable var16) {
                  if (var6 != null) {
                     try {
                        var6.close();
                     } catch (Throwable var10) {
                        var16.addSuppressed(var10);
                     }
                  }

                  throw var16;
               }

               if (var6 != null) {
                  var6.close();
               }
               break label109;
            }

            if (var6 != null) {
               var6.close();
            }

            return;
         }
      } catch (SQLException var17) {
         System.err.println("Error retrieving ticket info: " + var17.getMessage());
         return;
      }

      String var18 = "DELETE FROM Tickets WHERE ticket_id = ?";

      try {
         PreparedStatement var19 = this.connection.prepareStatement(var18);

         try {
            var19.setInt(1, var1);
            int var8 = var19.executeUpdate();
            if (var8 > 0) {
               System.out.println("Ticket removed.");
               this.updateTicketStatistics(var3, var4, "remove", var5);
            } else {
               System.out.println("Failed to remove ticket.");
            }
         } catch (Throwable var13) {
            if (var19 != null) {
               try {
                  var19.close();
               } catch (Throwable var12) {
                  var13.addSuppressed(var12);
               }
            }

            throw var13;
         }

         if (var19 != null) {
            var19.close();
         }
      } catch (SQLException var14) {
         System.err.println("Error removing ticket: " + var14.getMessage());
      }

   }

   private void updateTicketStatistics(int var1, String var2, String var3, String var4) throws SQLException {
      String var5 = "SELECT ticket_status, COUNT(*) AS count FROM Tickets WHERE event_id = ? AND ticket_type = ? GROUP BY ticket_status";
      boolean var6 = false;
      int var7 = 0;
      int var8 = 0;
      int var9 = 0;
      PreparedStatement var10 = this.connection.prepareStatement(var5);

      int var22;
      try {
         var10.setInt(1, var1);
         var10.setString(2, var2);
         ResultSet var11 = var10.executeQuery();

         try {
            while(var11.next()) {
               String var12 = var11.getString("ticket_status");
               int var13 = var11.getInt("count");
               switch (var12) {
                  case "Sold":
                     var7 = var13;
                     break;
                  case "Cancelled":
                     var8 = var13;
                     break;
                  case "Available":
                     var9 = var13;
               }
            }

            var22 = var7 + var8 + var9;
         } catch (Throwable var20) {
            if (var11 != null) {
               try {
                  var11.close();
               } catch (Throwable var17) {
                  var20.addSuppressed(var17);
               }
            }

            throw var20;
         }

         if (var11 != null) {
            var11.close();
         }
      } catch (Throwable var21) {
         if (var10 != null) {
            try {
               var10.close();
            } catch (Throwable var16) {
               var21.addSuppressed(var16);
            }
         }

         throw var21;
      }

      if (var10 != null) {
         var10.close();
      }

      String var23 = "UPDATE Ticket_Statistics SET total_tickets = ?, sold_tickets = ?, cancelled_tickets = ?, available_tickets = ? WHERE event_id = ? AND ticket_type = ?";
      PreparedStatement var24 = this.connection.prepareStatement(var23);

      try {
         var24.setInt(1, var22);
         var24.setInt(2, var7);
         var24.setInt(3, var8);
         var24.setInt(4, var9);
         var24.setInt(5, var1);
         var24.setString(6, var2);
         var24.executeUpdate();
      } catch (Throwable var19) {
         if (var24 != null) {
            try {
               var24.close();
            } catch (Throwable var18) {
               var19.addSuppressed(var18);
            }
         }

         throw var19;
      }

      if (var24 != null) {
         var24.close();
      }

      System.out.printf("Statistics Updated: Event %d, Type: %s, Total: %d (Sold: %d, Cancelled: %d, Available: %d)%n", var1, var2, var22, var7, var8, var9);
   }

   private void viewTicket() throws SQLException {
      String var1 = "SELECT * FROM Tickets";
      Statement var2 = this.connection.createStatement();

      try {
         ResultSet var3 = var2.executeQuery(var1);

         try {
            System.out.println("\nTicket Records:");

            while(var3.next()) {
               System.out.printf("ID: %d | Event: %d | Type: %s | Price: %.2f | Seat: %s | Status: %s%n", var3.getInt("ticket_id"), var3.getInt("event_id"), var3.getString("ticket_type"), var3.getBigDecimal("price"), var3.getString("seat_number"), var3.getString("ticket_status"));
            }
         } catch (Throwable var8) {
            if (var3 != null) {
               try {
                  var3.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }
            }

            throw var8;
         }

         if (var3 != null) {
            var3.close();
         }
      } catch (Throwable var9) {
         if (var2 != null) {
            try {
               var2.close();
            } catch (Throwable var6) {
               var9.addSuppressed(var6);
            }
         }

         throw var9;
      }

      if (var2 != null) {
         var2.close();
      }

   }

   private void viewTicketStatistics() throws SQLException {
      String var1 = "SELECT * FROM Ticket_Statistics";
      Statement var2 = this.connection.createStatement();

      try {
         ResultSet var3 = var2.executeQuery(var1);

         try {
            System.out.println("\nTicket Statistics:");

            while(var3.next()) {
               System.out.printf("Event: %d | Type: %s | Total: %d | Sold: %d | Cancelled: %d | Available: %d%n", var3.getInt("event_id"), var3.getString("ticket_type"), var3.getInt("total_tickets"), var3.getInt("sold_tickets"), var3.getInt("cancelled_tickets"), var3.getInt("available_tickets"));
            }
         } catch (Throwable var8) {
            if (var3 != null) {
               try {
                  var3.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }
            }

            throw var8;
         }

         if (var3 != null) {
            var3.close();
         }
      } catch (Throwable var9) {
         if (var2 != null) {
            try {
               var2.close();
            } catch (Throwable var6) {
               var9.addSuppressed(var6);
            }
         }

         throw var9;
      }

      if (var2 != null) {
         var2.close();
      }

   }
}
