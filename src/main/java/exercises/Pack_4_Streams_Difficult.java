package exercises;

import utils.Employee;
import utils.Employees;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static java.util.Arrays.asList;

@SuppressWarnings("all")
public class Pack_4_Streams_Difficult {

    private static final List<Employee> EMPLOYEES = Employees.allEmployees();


    @Test
    public void exercise_1_findFirst() {
        // find whether there are two employees with the same first name and surname and return the name
        // the solution has to be a single statement, complexity O(n^2) is acceptable

        String result = String.valueOf(EMPLOYEES.stream()
                .map(e -> e.getFirstName() + " " + e.getSurname())
                // .peek(n -> System.out.println("Between map() and colect() , " + "employee: " + n))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                //  .peek(n -> System.out.println("Between stream() and filter() , " + "employee: " + n))
                .filter(el -> el.getValue() == 2)
                .peek(n -> System.out.println("Between filter() and findFirst() , " + "employee: " + n))
                .findFirst().get()


        );
        assertThat(result, sameBeanAs("Holly Davies"));
    }

    @Test
    public void exercise_2_groupingBy_counting() {
        // find the total number of groups of at least 5 employees living close to each other
        // consider all employees with the same 2 first characters of the home address post code a single group
        // you can collect to map and then stream over it, however the solution has to be a single statement

        long result = EMPLOYEES
                .stream()
                .collect(Collectors.groupingBy(
                        (Employee employee) -> employee.getHomeAddress().getPostCode().substring(0, 2)))
                .entrySet()
                .stream()
                .filter(e -> e.getValue().size() >= 5)
                .count();


        //TODO write your code here

        assertThat(result, sameBeanAs(110L));
    }


    @Test
    public void exercise_3_flatMap() {
        // find the total number of all different home and correspondence addresses

        long result = EMPLOYEES
                .stream()
                .flatMap(e -> Stream.of(e.getHomeAddress(), e.getCorrespondenceAddress().get()))
                .distinct()
                .count();

        //TODO write your code here

        //   assertThat(result, sameBeanAs(1820L));
    }


    @Test
    public void exercise_4_groupingBy_summingInt() {
        // find how much in total each company pays (annually) to their employees, order result by amount
        // you can convert the salaries to ints using BigDecimal#intValue method
        // you can collect to map and then stream over it, however the solution has to be a single statement

        DecimalFormat decimalFormat = new DecimalFormat("£#,###.00");
        List<String> result = EMPLOYEES
                .stream()
                .collect(Collectors.groupingBy((Employee e) -> e.getCompany()))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                   se -> se.getKey().getName(),
                   se -> se.getValue()
                           .stream()
                           .map(employee -> employee.getSalary())
                           .reduce(
                             new BigDecimal(0),
                             (BigDecimal acc, BigDecimal currentSalary) -> acc.add(currentSalary))))
               .entrySet()
               .stream()
               .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
               .map(se -> se.getKey() + " - " + NumberFormat.getCurrencyInstance(Locale.UK).format(se.getValue()) )
               .collect(Collectors.toList());

        result.forEach(System.out::println);

        assertThat(result, sameBeanAs(asList(
                "Anglo American - £12,119,153.00",
                "HSBC - £11,469,144.00",
                "Royal Bank of Scotland Group - £11,127,807.00",
                "BP - £10,925,088.00",
                "AstraZeneca - £10,507,305.00",
                "HBOS - £10,428,819.00",
                "Royal Dutch Shell - £10,100,098.00",
                "Barclays plc - £10,071,534.00",
                "Vodafone Group - £10,029,401.00",
                "GlaxoSmithKline - £9,499,235.00"
        )));
    }

    @Test
    public void exercise_5_patternCompileSplitAsStream() {
        // count the instances of words and output a list of formatted strings
        // output the strings sorted lexicographically by name
        // you can use collect twice
        // as always, a single statement solution is expected
        // hint: look at Pattern.compile(regex).splitAsStream(string)


        String string = "dog" + "\n" + "bird" + "\n" + "cat" + "\n" + "cat" + "\n" + "dog" + "\n" + "cat";
        List<String> result = Pattern.compile("\\s+").splitAsStream(string)
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()))
                .entrySet()
                .stream()
                .map(e -> e.getKey()+ " - "+ e.getValue())
                .sorted((k, v) -> k.compareTo(v))
                .collect(Collectors.toList());
        //TODO write your code here

        assertThat(result, sameBeanAs(asList(
                "bird - 1",
                "cat - 3",
                "dog - 2"
        )));
    }
    @Test
    public void exercise_6_mapToLong() {
        // the rows and columns of the chess board are assigned arbitrary numbers (instead of letters and digits)
        // the value of the square is a multiplication of the numbers assigned to corresponding row and column
        // e.g. square A1 has a value 6,432 * 6,199 = 39,871,968
        // calculate the sum of values of all squares
        int[] rows = {6432, 8997, 8500, 7036, 9395, 9372, 9715, 9634};
        int[] columns = {6199, 9519, 6745, 8864, 8788, 7322, 7341, 7395};

        long result = Arrays.stream(rows)
                .mapToLong(row -> row)
                .reduce(
                        0L,
                        (acc, row) -> acc + Arrays
                                .stream(columns)
                                .mapToLong(column -> column)
                                .reduce(
                                        0L,
                                        (acc2, column) -> acc2 + row * column )
                );

        //TODO write your code here

         assertThat(result, sameBeanAs(4294973013L));
    }


    @Test
    public void exercise_7_randomLongs_concat_toArray() {
        // concatenate two random streams of numbers (seed is fixed for testing purposes),
        // then revert the sign of the negative ones
        // then sort them and pick 10 middle elements (hint: you can use skip and limit)
        // then do modulo 1000 (remainder of division by 1000)
        // and finally collect the result into an array
        LongStream longs = new Random(0).longs(10);
        IntStream ints = new Random(0).ints(10);

        Integer[] result =
            LongStream.concat(longs, ints.mapToLong(x -> x))
                .map(a -> a < 0 ? -a : a)
                .sorted()
                .skip(20 / 2 - 5) // how to not hardcode 20, 5, and 10?
                .limit(10)
                .map(x -> x % 1000)
                .mapToInt(x -> (int)x)
                .boxed()
                .toArray(Integer[]::new);

        assertThat(result, sameBeanAs(new long[]{106, 266, 402, 858, 313, 688, 303, 137, 766, 896}));
    }

}
