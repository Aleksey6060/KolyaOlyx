import 'dart:io';
import 'dart:math';
import 'dart:async';
import 'dart:isolate';

const String emptyCell = '-';
const String shotMiss = 'X';
const String shotHit = '*';
const String shipMark = 'X';
const String aroundSunk = 'X';

const int fieldSize = 10;
final List<int> fleet = [4, 3, 3, 2, 2, 2, 1, 1, 1, 1];

enum FieldCell { clear, vessel, damaged, emptyShot, sunkZone }

enum PlayType { vsComputer, vsHuman }

class PlacementJob {
  final int size;
  final List<int> lengths;
  final int randomSeed;
  PlacementJob(this.size, this.lengths, this.randomSeed);
}

class PlacementData {
  final List<Map<String, dynamic>> vessels;
  final List<List<FieldCell>> cells;
  PlacementData(this.vessels, this.cells);
}

class BattleStats {
  String playerName;
  int successful = 0;
  int failed = 0;
  int enemySunk = 0;
  int ownLost = 0;
  int remaining = 0;
  int shotsTotal = 0;

  BattleStats(this.playerName);

  @override
  String toString() =>
      '''
$playerName:
  - Уничтожено вражеских: $enemySunk
  - Потеряно своих: $ownLost
  - Осталось своих: $remaining
  - Выстрелов всего: $shotsTotal
  - Попаданий: $successful
  - Промахов: $failed
  - Точность: ${shotsTotal > 0 ? (successful / shotsTotal * 100).toStringAsFixed(1) : 0}%
''';
}

class GameField {
  late List<List<FieldCell>> cells;
  late List<Vessel> fleetUnits;
  int unitsLeft = fleet.length;

  GameField() {
    cells = List.generate(
      fieldSize,
      (_) => List.filled(fieldSize, FieldCell.clear),
    );
    fleetUnits = [];
  }

  GameField.fromData(PlacementData data) {
    cells = data.cells;
    fleetUnits = [];
    for (var unit in data.vessels) {
      fleetUnits.add(
        Vessel(unit['row'], unit['col'], unit['size'], unit['isHorizontal']),
      );
    }
    unitsLeft = fleetUnits.length;
  }

  static Future<GameField> generateAsync({int? seed}) async {
    final port = ReceivePort();
    final job = PlacementJob(
      fieldSize,
      fleet,
      seed ?? Random().nextInt(999999),
    );
    await Isolate.spawn(_generatePlacement, [port.sendPort, job]);
    final data = await port.first as PlacementData;
    port.close();
    return GameField.fromData(data);
  }

  static void _generatePlacement(List args) {
    final output = args[0] as SendPort;
    final job = args[1] as PlacementJob;
    final rnd = Random(job.randomSeed);
    final units = <Map<String, dynamic>>[];
    final matrix = List.generate(
      job.size,
      (_) => List.filled(job.size, FieldCell.clear),
    );

    for (var length in job.lengths) {
      while (true) {
        final r = rnd.nextInt(job.size);
        final c = rnd.nextInt(job.size);
        final horiz = rnd.nextBool();
        if (_canPlace(matrix, r, c, length, horiz, job.size)) {
          _putUnit(matrix, units, r, c, length, horiz);
          break;
        }
      }
    }
    output.send(PlacementData(units, matrix));
  }

  static bool _canPlace(
    List<List<FieldCell>> m,
    int r,
    int c,
    int len,
    bool h,
    int s,
  ) {
    if (h) {
      if (c + len > s) return false;
      for (var i = 0; i < len; i++) {
        if (m[r][c + i] != FieldCell.clear) return false;
        for (var dr = -1; dr <= 1; dr++) {
          for (var dc = -1; dc <= 1; dc++) {
            final nr = r + dr;
            final nc = c + i + dc;
            if (nr >= 0 &&
                nr < s &&
                nc >= 0 &&
                nc < s &&
                m[nr][nc] == FieldCell.vessel) {
              return false;
            }
          }
        }
      }
    } else {
      if (r + len > s) return false;
      for (var i = 0; i < len; i++) {
        if (m[r + i][c] != FieldCell.clear) return false;
        for (var dr = -1; dr <= 1; dr++) {
          for (var dc = -1; dc <= 1; dc++) {
            final nr = r + i + dr;
            final nc = c + dc;
            if (nr >= 0 &&
                nr < s &&
                nc >= 0 &&
                nc < s &&
                m[nr][nc] == FieldCell.vessel) {
              return false;
            }
          }
        }
      }
    }
    return true;
  }

  static void _putUnit(
    List<List<FieldCell>> m,
    List<Map<String, dynamic>> u,
    int r,
    int c,
    int len,
    bool h,
  ) {
    u.add({'row': r, 'col': c, 'size': len, 'isHorizontal': h});
    if (h) {
      for (var i = 0; i < len; i++) m[r][c + i] = FieldCell.vessel;
    } else {
      for (var i = 0; i < len; i++) m[r + i][c] = FieldCell.vessel;
    }
  }

  void scatterRandomly() {
    final r = Random();
    for (var len in fleet) {
      while (true) {
        final row = r.nextInt(fieldSize);
        final col = r.nextInt(fieldSize);
        final horiz = r.nextBool();
        if (canPlaceUnit(row, col, len, horiz)) {
          addUnit(row, col, len, horiz);
          break;
        }
      }
    }
  }

  bool canPlaceUnit(int r, int c, int len, bool h) {
    // аналогично _canPlace, но без префикса _
    if (h) {
      if (c + len > fieldSize) return false;
      for (var i = 0; i < len; i++) {
        if (cells[r][c + i] != FieldCell.clear) return false;
        for (var dr = -1; dr <= 1; dr++) {
          for (var dc = -1; dc <= 1; dc++) {
            final nr = r + dr;
            final nc = c + i + dc;
            if (nr >= 0 &&
                nr < fieldSize &&
                nc >= 0 &&
                nc < fieldSize &&
                cells[nr][nc] == FieldCell.vessel)
              return false;
          }
        }
      }
    } else {
      if (r + len > fieldSize) return false;
      for (var i = 0; i < len; i++) {
        if (cells[r + i][c] != FieldCell.clear) return false;
        for (var dr = -1; dr <= 1; dr++) {
          for (var dc = -1; dc <= 1; dc++) {
            final nr = r + i + dr;
            final nc = c + dc;
            if (nr >= 0 &&
                nr < fieldSize &&
                nc >= 0 &&
                nc < fieldSize &&
                cells[nr][nc] == FieldCell.vessel)
              return false;
          }
        }
      }
    }
    return true;
  }

  void addUnit(int r, int c, int len, bool h) {
    fleetUnits.add(Vessel(r, c, len, h));
    if (h) {
      for (var i = 0; i < len; i++) cells[r][c + i] = FieldCell.vessel;
    } else {
      for (var i = 0; i < len; i++) cells[r + i][c] = FieldCell.vessel;
    }
  }

  bool fireAt(int r, int c) {
    if (r < 0 || r >= fieldSize || c < 0 || c >= fieldSize) return false;
    final st = cells[r][c];
    if (st == FieldCell.damaged ||
        st == FieldCell.emptyShot ||
        st == FieldCell.sunkZone)
      return false;

    if (st == FieldCell.vessel) {
      cells[r][c] = FieldCell.damaged;
      final unit = findUnit(r, c);
      if (unit != null) {
        unit.damage++;
        if (unit.destroyed) {
          unitsLeft--;
          surroundDestroyed(unit);
        }
      }
      return true;
    } else {
      cells[r][c] = FieldCell.emptyShot;
      return false;
    }
  }

  Vessel? findUnit(int r, int c) {
    for (final u in fleetUnits) {
      if (u.covers(r, c)) return u;
    }
    return null;
  }

  void surroundDestroyed(Vessel u) {
    final minR = max(0, u.startRow - 1);
    final maxR = min(
      fieldSize - 1,
      u.startRow + (u.horizontal ? 0 : u.length - 1) + 1,
    );
    final minC = max(0, u.startCol - 1);
    final maxC = min(
      fieldSize - 1,
      u.startCol + (u.horizontal ? u.length - 1 : 0) + 1,
    );

    for (var row = minR; row <= maxR; row++) {
      for (var col = minC; col <= maxC; col++) {
        if (cells[row][col] == FieldCell.clear) {
          cells[row][col] = FieldCell.sunkZone;
        }
      }
    }
  }

  void printField(bool revealShips, {String header = ''}) {
    if (header.isNotEmpty) print('=== $header ===');
    print('  A B C D E F G H I J');
    for (var i = 0; i < fieldSize; i++) {
      stdout.write('${i + 1 < 10 ? ' ' : ''}${i + 1}');
      for (var j = 0; j < fieldSize; j++) {
        final cell = cells[i][j];
        String ch;
        if (cell == FieldCell.clear)
          ch = emptyCell;
        else if (cell == FieldCell.vessel)
          ch = revealShips ? shipMark : emptyCell;
        else if (cell == FieldCell.damaged)
          ch = shotHit;
        else
          ch = shotMiss;
        stdout.write(' $ch');
      }
      print('');
    }
    print('');
  }

  bool allSunk() => unitsLeft == 0;
}

class Vessel {
  final int startRow;
  final int startCol;
  final int length;
  final bool horizontal;
  int damage = 0;

  Vessel(this.startRow, this.startCol, this.length, this.horizontal);

  bool covers(int r, int c) {
    if (horizontal)
      return r == startRow && c >= startCol && c < startCol + length;
    return c == startCol && r >= startRow && r < startRow + length;
  }

  bool get destroyed => damage == length;
}

abstract class Participant {
  String label;
  GameField myField;
  GameField enemyField;
  BattleStats record;
  final StreamController<String> notifier;

  Participant(this.label)
    : myField = GameField(),
      enemyField = GameField(),
      record = BattleStats(label),
      notifier = StreamController<String>.broadcast();

  Stream<String> get notifications => notifier.stream;

  void arrangeFleet();
  Future<List<int>> chooseTarget();
  Future<bool> performShot(Participant target);

  void recordShot(bool success, bool sunk, Participant opp) {
    record.shotsTotal++;
    if (success) {
      record.successful++;
      if (sunk) {
        record.enemySunk++;
        opp.record.ownLost++;
      }
    } else {
      record.failed++;
    }
  }

  void cleanup() => notifier.close();
}

class Person extends Participant {
  Person(String name) : super(name);

  @override
  void arrangeFleet() {
    print('$label — расстановка:');
    print('1 — вручную    2 — случайно');
    final ans = stdin.readLineSync()!.trim();
    if (ans == '2') {
      myField.scatterRandomly();
      myField.printField(true, header: 'Ваше расположение');
    } else if (ans == '1') {
      myField.printField(true, header: 'Ваше расположение');
      for (var len in fleet) {
        var ok = false;
        while (!ok) {
          print('Корабль $len клеток. Начало (A1–J10):');
          final pos = _readPosition();
          var dir = true;
          if (len > 1) {
            print('1 — горизонтально   2 — вертикально');
            dir = stdin.readLineSync()!.trim() == '1';
          }
          if (myField.canPlaceUnit(pos[0], pos[1], len, dir)) {
            myField.addUnit(pos[0], pos[1], len, dir);
            ok = true;
            myField.printField(true, header: 'Ваше расположение');
          } else {
            print('Невозможно поставить. Повторите.');
          }
        }
      }
    } else {
      myField.scatterRandomly();
      myField.printField(true, header: 'Ваше расположение');
    }
  }

  List<int> _readPosition() {
    while (true) {
      try {
        final txt = stdin.readLineSync()!.trim().toUpperCase();
        if (txt.length < 2) continue;
        final c = txt.codeUnitAt(0) - 'A'.codeUnitAt(0);
        final r = int.parse(txt.substring(1)) - 1;
        if (r >= 0 && r < fieldSize && c >= 0 && c < fieldSize) return [r, c];
      } catch (_) {}
      print('Ошибка. Пример: A5, J10');
    }
  }

  @override
  Future<List<int>> chooseTarget() async {
    await Future.delayed(Duration(milliseconds: 120));
    while (true) {
      print('$label — цель (A1–J10):');
      try {
        final txt = stdin.readLineSync()!.trim().toUpperCase();
        if (txt.length < 2) continue;
        final c = txt.codeUnitAt(0) - 'A'.codeUnitAt(0);
        final r = int.parse(txt.substring(1)) - 1;
        if (r >= 0 && r < fieldSize && c >= 0 && c < fieldSize) return [r, c];
      } catch (_) {}
      print('Неверный формат');
    }
  }

  @override
  Future<bool> performShot(Participant target) async {
    myField.printField(true, header: 'Моё поле — $label');
    enemyField.printField(false, header: 'Поле противника');
    final pos = await chooseTarget();
    final success = target.myField.fireAt(pos[0], pos[1]);
    final unit = target.myField.findUnit(pos[0], pos[1]);
    final sunk = unit != null && unit.destroyed;
    recordShot(success, sunk, target);

    enemyField.cells[pos[0]][pos[1]] = success
        ? FieldCell.damaged
        : FieldCell.emptyShot;

    if (success && sunk) {
      print('Потоплен! Осталось у противника: ${target.myField.unitsLeft}');
      final u = unit!;
      final minR = max(0, u.startRow - 1);
      final maxR = min(
        fieldSize - 1,
        u.startRow + (u.horizontal ? 0 : u.length - 1) + 1,
      );
      final minC = max(0, u.startCol - 1);
      final maxC = min(
        fieldSize - 1,
        u.startCol + (u.horizontal ? u.length - 1 : 0) + 1,
      );
      for (var rr = minR; rr <= maxR; rr++) {
        for (var cc = minC; cc <= maxC; cc++) {
          if (enemyField.cells[rr][cc] == FieldCell.clear) {
            enemyField.cells[rr][cc] = FieldCell.sunkZone;
          }
        }
      }
    }

    notifier.add(
      success ? 'Попадание $pos[0]:$pos[1]' : 'Мимо $pos[0]:$pos[1]',
    );
    print(success ? 'Попадание!' : 'Мимо!');
    return success;
  }
}

class Computer extends Participant {
  final rnd = Random();
  final Set<String> used = {};

  Computer() : super('Компьютер');

  @override
  void arrangeFleet() => myField.scatterRandomly();

  @override
  Future<List<int>> chooseTarget() async {
    await Future.delayed(Duration(milliseconds: 450));
    int r, c;
    do {
      r = rnd.nextInt(fieldSize);
      c = rnd.nextInt(fieldSize);
    } while (used.contains('$r-$c'));
    return [r, c];
  }

  @override
  Future<bool> performShot(Participant target) async {
    final pos = await chooseTarget();
    final hit = target.myField.fireAt(pos[0], pos[1]);
    final unit = target.myField.findUnit(pos[0], pos[1]);
    final sunk = unit != null && unit.destroyed;
    recordShot(hit, sunk, target);

    used.add('${pos[0]}-${pos[1]}');

    if (hit && sunk)
      print('Компьютер потопил корабль! Осталось: ${target.myField.unitsLeft}');

    notifier.add(
      hit
          ? 'Компьютер попал ${pos[0]}:${pos[1]}'
          : 'Компьютер промахнулся ${pos[0]}:${pos[1]}',
    );
    print(
      'Компьютер → ${String.fromCharCode('A'.codeUnitAt(0) + pos[1])}${pos[0] + 1}: ${hit ? 'попадание' : 'мимо'}',
    );
    return hit;
  }
}

class Match {
  Participant first;
  Participant? second;
  PlayType variant;
  int scoreFirst = 0;
  int scoreSecond = 0;
  int rounds = 0;

  final StreamController<String> broadcast =
      StreamController<String>.broadcast();
  Stream<String> get broadcasts => broadcast.stream;

  Match(this.variant) : first = Person('Игрок 1') {
    second = variant == PlayType.vsComputer ? Computer() : Person('Игрок 2');

    first.notifications.listen((e) => broadcast.add('${first.label}: $e'));
    second!.notifications.listen((e) => broadcast.add('${second!.label}: $e'));
  }

  Future<void> start() async {
    first.record = BattleStats(first.label);
    second!.record = BattleStats(second!.label);

    print('Расстановка флота...');
    await _setupFields();

    var active = first;
    var passive = second!;

    while (true) {
      final struck = await active.performShot(passive);
      if (first.myField.allSunk() || second!.myField.allSunk()) break;
      if (!struck) {
        final swap = active;
        active = passive;
        passive = swap;
        print('Ход → ${active.label}');
        await Future.delayed(Duration(milliseconds: 600));
      }
    }

    first.record.remaining = first.myField.unitsLeft;
    second!.record.remaining = second!.myField.unitsLeft;

    final victorious = first.myField.allSunk() ? second! : first;
    print('${victorious.label} победил!');

    if (victorious == first)
      scoreFirst++;
    else
      scoreSecond++;

    rounds++;
    showScore();
    showRecords();
    await _exportRecords();
  }

  Future<void> _setupFields() async {
    first.arrangeFleet();
    if (second is Computer) {
      second!.myField = await GameField.generateAsync();
      second!.myField.printField(true, header: 'Поле компьютера');
    } else {
      second!.arrangeFleet();
    }
  }

  void showScore() {
    print('Счёт');
    print('Партий: $rounds');
    print('${first.label}: $scoreFirst');
    print('${second!.label}: $scoreSecond\n');
  }

  void showRecords() {
    print('Статистика');
    print(first.record);
    print(second!.record);
  }

  Future<void> _exportRecords() async {
    try {
      final dir = Directory('battle_logs');
      if (!dir.existsSync()) await dir.create();

      final t = DateTime.now();
      final fname =
          'log_${t.year}-${t.month}-${t.day}_${t.hour}-${t.minute}-${t.second}.txt';
      final f = File('battle_logs/$fname');

      await f.writeAsString('''
МОРСКОЙ БОЙ – протокол
${t.toString()}
Режим: ${variant == PlayType.vsComputer ? 'vs ИИ' : 'vs человек'}

${first.record}

${second!.record}

Счёт:
$rounds партий
${first.label}: $scoreFirst
${second!.label}: $scoreSecond
''');
      print('Сохранено: battle_logs/$fname');
    } catch (err) {
      print('Ошибка записи: $err');
    }
  }

  Future<bool> continueGame() async {
    print('Ещё раз? д/н');
    final ans = await _readAsync();
    return ans.toLowerCase() == 'д';
  }

  Future<String> _readAsync() => Future.value(stdin.readLineSync()!.trim());

  void release() {
    first.cleanup();
    second!.cleanup();
    broadcast.close();
  }
}

void main() async {
  print('Морской бой');
  print('1 – против ИИ    2 – два игрока');

  final choice = await _readAsync();
  final type = choice == '1' ? PlayType.vsComputer : PlayType.vsHuman;

  try {
    final session = Match(type);
    session.broadcasts.listen((_) {});

    var playing = true;
    while (playing) {
      await session.start();
      playing = await session.continueGame();
    }
    session.release();
  } catch (e) {
    print('Сбой: $e');
  }
}

Future<String> _readAsync() => Future.value(stdin.readLineSync()!.trim());
