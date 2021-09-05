import { procedure as racelistProcedure } from "./racelist.ts";
import { procedure as calendarProcedure } from "./calendar.ts";
import { procedure as resultProcedure } from "./result.ts";
import { writeCSV } from "https://deno.land/x/csv@v0.5.1/mod.ts";
import { existsSync } from "https://deno.land/std/fs/mod.ts";

const writeHorseDataCsv = async (
	dirName: string,
	fileName: string,
  csv: string[][],
) => {
  const f = await Deno.open(`./out/${dirName}/${fileName}.csv`, {
    write: true,
    create: true,
    truncate: true,
  });
  await writeCSV(f, csv);
  f.close();
};

if (Deno.args.length !== 2) {
  throw new Error(
    "usage: deno run --allow-net --allow-write ./src/main.ts <year> <month>",
  );
}
const year = Deno.args[0];
const month = Deno.args[1];

if (!existsSync(`./out/${year}${month}`)) Deno.mkdirSync(`./out/${year}${month}`)

const eventDates = await calendarProcedure(year, month);
const raceIds =
  (await Promise.all(
    eventDates.map(async (eventDate) => await racelistProcedure(eventDate)),
  )).flat(2);

raceIds.forEach(async (raceId) => {
  if (!raceId) return;

  const result = await resultProcedure(raceId);
  writeHorseDataCsv(`${year}${month}`, raceId, result);
});
