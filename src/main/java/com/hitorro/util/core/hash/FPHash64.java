/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.hitorro.util.core.hash;

import com.hitorro.util.core.Log;

import java.io.UnsupportedEncodingException;

/**
 * <p/>
 * Java convertion of Andrei Broaders' Fingerprint algorithm, which is an re-implementation of a rabin algorithm.
 * <p/>
 * http://modula3.elegosoft.com/cm3/doc/help/gen_html/m3core/src/fingerprint/Fingerprint.i3.html
 * <p/>
 * C version:
 * <p/>
 * http://gcc.gnu.org/ml/gcc/1999-11n/msg00592.html
 * <p/>
 * This algorithm has a number of advantages of say MD5: 1 )supports merging of hashes (take two bodies of bytes,
 * compute hash for each part, apply the hash, hash is equiv to that computed if you simply hashed the two bodies of
 * bytes in a contiguous way. 2) Fast. 3) extreemly low collision rate.  Can be used for such things as dictionary keys
 * to save memory.
 */
public class FPHash64 {

    private static final long PolynomialOne = (((-0x7fffffffL - 1L) << 32) | (0L & 0xffffffffL));
    /**
     * *****************************************************
     */
    /* Polynomial Constants for the POLY module              */

    private static final int FINGERPRINT_A = 0xff208489;
    private static final int FINGERPRINT_B = 0xf4872e10;
    private static final int FINGERPRINT_C = 0x402d619b;
    private static final int FINGERPRINT_D = 0x0bf359a7;
    private static final int fingerprint_perm[]
            = {55, 254, 252, 251, 250, 248, 240, 245,
            246, 238, 237, 244, 7, 189, 214, 236,
            235, 20, 33, 8, 227, 14, 233, 178,
            172, 60, 229, 133, 152, 19, 210, 203,
            221, 208, 76, 18, 13, 199, 113, 62,
            40, 190, 213, 194, 43, 181, 21, 15,
            201, 162, 90, 186, 71, 117, 107, 70,
            191, 5, 173, 44, 39, 12, 174, 183,
            99, 11, 176, 163, 161, 72, 86, 105,
            2, 83, 42, 52, 179, 135, 103, 110,
            151, 58, 108, 96, 166, 25, 115, 66,
            142, 10, 141, 48, 104, 34, 159, 120,
            22, 140, 64, 82, 78, 68, 207, 125,
            123, 150, 144, 138, 128, 139, 136, 114,
            119, 53, 148, 185, 41, 124, 216, 143,
            49, 92, 98, 51, 112, 73, 50, 63,
            16, 46, 158, 126, 206, 122, 94, 132,
            88, 184, 28, 84, 127, 156, 167, 223,
            118, 89, 116, 17, 111, 121, 109, 77,
            146, 61, 224, 101, 81, 218, 97, 188,
            243, 155, 57, 102, 54, 129, 93, 192,
            153, 106, 36, 145, 79, 31, 137, 26,
            67, 85, 175, 80, 168, 65, 91, 1,
            147, 149, 6, 29, 37, 69, 182, 165,
            4, 74, 55, 47, 171, 169, 75, 134,
            193, 195, 198, 131, 38, 180, 56, 196,
            23, 154, 177, 200, 205, 27, 209, 95,
            204, 160, 3, 30, 157, 32, 9, 212,
            211, 45, 202, 170, 0, 219, 187, 87,
            35, 100, 217, 232, 164, 228, 220, 197,
            231, 215, 226, 130, 225, 234, 241, 239,
            59, 230, 247, 24, 249, 242, 222, 253
    };
    private static final long Polynomial64[] = {((0L << 32) | (0L & 0xffffffffL)),
            ((152935311L << 32) | (36728807L & 0xffffffffL)),
            ((305870622L << 32) | (73457614L & 0xffffffffL)),
            ((455519377L << 32) | (105951273L & 0xffffffffL)),
            ((386180924L << 32) | (85802743L & 0xffffffffL)),
            ((504970419L << 32) | (120410384L & 0xffffffffL)),
            ((88051746L << 32) | (25026873L & 0xffffffffL)),
            ((203557805L << 32) | (55414494L & 0xffffffffL)),
            ((494634872L << 32) | (132578437L & 0xffffffffL)),
            ((342236407L << 32) | (97948514L & 0xffffffffL)),
            ((255897702L << 32) | (59122507L & 0xffffffffL)),
            ((106769385L << 32) | (28724396L & 0xffffffffL)),
            ((176103492L << 32) | (50053746L & 0xffffffffL)),
            ((56802251L << 32) | (13348245L & 0xffffffffL)),
            ((407115610L << 32) | (110828988L & 0xffffffffL)),
            ((291081429L << 32) | (78344795L & 0xffffffffL)),
            ((159762416L << 32) | (34699361L & 0xffffffffL)),
            ((9985151L << 32) | (2168710L & 0xffffffffL)),
            ((465498350L << 32) | (108111791L & 0xffffffffL)),
            ((312699745L << 32) | (71419976L & 0xffffffffL)),
            ((511795404L << 32) | (118245014L & 0xffffffffL)),
            ((396163907L << 32) | (87828849L & 0xffffffffL)),
            ((213538770L << 32) | (57448792L & 0xffffffffL)),
            ((94882909L << 32) | (22869695L & 0xffffffffL)),
            ((352206984L << 32) | (100107492L & 0xffffffffL)),
            ((501472007L << 32) | (130542339L & 0xffffffffL)),
            ((113604502L << 32) | (26696490L & 0xffffffffL)),
            ((265874457L << 32) | (61289677L & 0xffffffffL)),
            ((66774964L << 32) | (15383059L & 0xffffffffL)),
            ((182942779L << 32) | (47896052L & 0xffffffffL)),
            ((297914538L << 32) | (76178909L & 0xffffffffL)),
            ((417090341L << 32) | (112855610L & 0xffffffffL)),
            ((319524832L << 32) | (69398722L & 0xffffffffL)),
            ((437654639L << 32) | (101883685L & 0xffffffffL)),
            ((19970302L << 32) | (4337420L & 0xffffffffL)),
            ((137175921L << 32) | (41042155L & 0xffffffffL)),
            ((68102364L << 32) | (20958773L & 0xffffffffL)),
            ((219329363L << 32) | (51356114L & 0xffffffffL)),
            ((372514754L << 32) | (90116603L & 0xffffffffL)),
            ((522814541L << 32) | (124747292L & 0xffffffffL)),
            ((242223256L << 32) | (63179847L & 0xffffffffL)),
            ((124621591L << 32) | (32793504L & 0xffffffffL)),
            ((474693510L << 32) | (128242569L & 0xffffffffL)),
            ((357999625L << 32) | (93633646L & 0xffffffffL)),
            ((427077540L << 32) | (114897584L & 0xffffffffL)),
            ((275330091L << 32) | (82402647L & 0xffffffffL)),
            ((189765818L << 32) | (45739390L & 0xffffffffL)),
            ((38929205L << 32) | (9011865L & 0xffffffffL)),
            ((445532176L << 32) | (104040611L & 0xffffffffL)),
            ((328455071L << 32) | (67364676L & 0xffffffffL)),
            ((146112270L << 32) | (39016301L & 0xffffffffL)),
            ((27845761L << 32) | (6502538L & 0xffffffffL)),
            ((227209004L << 32) | (53392980L & 0xffffffffL)),
            ((77034659L << 32) | (18799027L & 0xffffffffL)),
            ((531748914L << 32) | (122579354L & 0xffffffffL)),
            ((380388285L << 32) | (92145277L & 0xffffffffL)),
            ((133549928L << 32) | (30766118L & 0xffffffffL)),
            ((250107111L << 32) | (65346497L & 0xffffffffL)),
            ((365885558L << 32) | (95792104L & 0xffffffffL)),
            ((483615737L << 32) | (126206991L & 0xffffffffL)),
            ((284256340L << 32) | (80235217L & 0xffffffffL)),
            ((434959323L << 32) | (116925750L & 0xffffffffL)),
            ((46817098L << 32) | (11048223L & 0xffffffffL)),
            ((198689989L << 32) | (43580152L & 0xffffffffL)),
            ((358995648L << 32) | (93937903L & 0xffffffffL)),
            ((477646159L << 32) | (128519944L & 0xffffffffL)),
            ((123658718L << 32) | (33144609L & 0xffffffffL)),
            ((239303249L << 32) | (63557830L & 0xffffffffL)),
            ((39940604L << 32) | (8674840L & 0xffffffffL)),
            ((192735859L << 32) | (45363711L & 0xffffffffL)),
            ((274351842L << 32) | (82084310L & 0xffffffffL)),
            ((424140141L << 32) | (114617905L & 0xffffffffL)),
            ((136204728L << 32) | (41917546L & 0xffffffffL)),
            ((17025591L << 32) | (5239693L & 0xffffffffL)),
            ((438658726L << 32) | (102712228L & 0xffffffffL)),
            ((322501929L << 32) | (70200387L & 0xffffffffL)),
            ((521860740L << 32) | (123904669L & 0xffffffffL)),
            ((369585419L << 32) | (89312634L & 0xffffffffL)),
            ((220316058L << 32) | (50494803L & 0xffffffffL)),
            ((71064085L << 32) | (20058804L & 0xffffffffL)),
            ((484446512L << 32) | (126359694L & 0xffffffffL)),
            ((368937663L << 32) | (95975273L & 0xffffffffL)),
            ((249243182L << 32) | (65587008L & 0xffffffffL)),
            ((130465185L << 32) | (30976167L & 0xffffffffL)),
            ((199538188L << 32) | (43329145L & 0xffffffffL)),
            ((49884547L << 32) | (10832286L & 0xffffffffL)),
            ((434077970L << 32) | (116783543L & 0xffffffffL)),
            ((281156253L << 32) | (80057936L & 0xffffffffL)),
            ((26973768L << 32) | (7267339L & 0xffffffffL)),
            ((143003079L << 32) | (39750636L & 0xffffffffL)),
            ((329294166L << 32) | (68041669L & 0xffffffffL)),
            ((448608985L << 32) | (104748066L & 0xffffffffL)),
            ((379531636L << 32) | (91478780L & 0xffffffffL)),
            ((528657147L << 32) | (121877787L & 0xffffffffL)),
            ((77858410L << 32) | (18023730L & 0xffffffffL)),
            ((230268389L << 32) | (52652757L & 0xffffffffL)),
            ((107895072L << 32) | (29069357L & 0xffffffffL)),
            ((259244719L << 32) | (59506634L & 0xffffffffL)),
            ((341143102L << 32) | (98258915L & 0xffffffffL)),
            ((491320753L << 32) | (132849668L & 0xffffffffL)),
            ((292224540L << 32) | (78032602L & 0xffffffffL)),
            ((410477971L << 32) | (110543165L & 0xffffffffL)),
            ((55691522L << 32) | (13005076L & 0xffffffffL)),
            ((172774029L << 32) | (49684211L & 0xffffffffL)),
            ((454418008L << 32) | (106785960L & 0xffffffffL)),
            ((302532055L << 32) | (74253135L & 0xffffffffL)),
            ((154069318L << 32) | (37598054L & 0xffffffffL)),
            ((3371721L << 32) | (908417L & 0xffffffffL)),
            ((202471780L << 32) | (54547039L & 0xffffffffL)),
            ((84730603L << 32) | (24133048L & 0xffffffffL)),
            ((506089082L << 32) | (119573905L & 0xffffffffL)),
            ((389535221L << 32) | (84992630L & 0xffffffffL)),
            ((267099856L << 32) | (61532236L & 0xffffffffL)),
            ((116786527L << 32) | (26904491L & 0xffffffffL)),
            ((500214222L << 32) | (130692994L & 0xffffffffL)),
            ((348992065L << 32) | (100292709L & 0xffffffffL)),
            ((418331116L << 32) | (112711355L & 0xffffffffL)),
            ((301113955L << 32) | (76003676L & 0xffffffffL)),
            ((181669618L << 32) | (47647093L & 0xffffffffL)),
            ((63542653L << 32) | (15165074L & 0xffffffffL)),
            ((311433640L << 32) | (72094921L & 0xffffffffL)),
            ((462258727L << 32) | (108821294L & 0xffffffffL)),
            ((11218614L << 32) | (2935559L & 0xffffffffL)),
            ((162968889L << 32) | (35431648L & 0xffffffffL)),
            ((93634196L << 32) | (22096446L & 0xffffffffL)),
            ((210314523L << 32) | (56706521L & 0xffffffffL)),
            ((397379978L << 32) | (87160304L & 0xffffffffL)),
            ((514986501L << 32) | (117545495L & 0xffffffffL)),
            ((431580288L << 32) | (116277429L & 0xffffffffL)),
            ((279248655L << 32) | (81669970L & 0xffffffffL)),
            ((193090462L << 32) | (42901371L & 0xffffffffL)),
            ((44025873L << 32) | (12513436L & 0xffffffffL)),
            ((247317436L << 32) | (66289218L & 0xffffffffL)),
            ((127948851L << 32) | (29561253L & 0xffffffffL)),
            ((478606498L << 32) | (127115660L & 0xffffffffL)),
            ((362508077L << 32) | (94621291L & 0xffffffffL)),
            ((79881208L << 32) | (17349680L & 0xffffffffL)),
            ((232749175L << 32) | (54055895L & 0xffffffffL)),
            ((385471718L << 32) | (90727422L & 0xffffffffL)),
            ((535056233L << 32) | (123210777L & 0xffffffffL)),
            ((331760836L << 32) | (68518599L & 0xffffffffL)),
            ((450617163L << 32) | (103148832L & 0xffffffffL)),
            ((33387482L << 32) | (7691529L & 0xffffffffL)),
            ((148957269L << 32) | (38089454L & 0xffffffffL)),
            ((272409456L << 32) | (83835092L & 0xffffffffL)),
            ((421607679L << 32) | (114251571L & 0xffffffffL)),
            ((34051182L << 32) | (10479386L & 0xffffffffL)),
            ((186257377L << 32) | (45058301L & 0xffffffffL)),
            ((121111628L << 32) | (31589923L & 0xffffffffL)),
            ((237346755L << 32) | (64121284L & 0xffffffffL)),
            ((352531282L << 32) | (92461549L & 0xffffffffL)),
            ((471771357L << 32) | (129152522L & 0xffffffffL)),
            ((222766088L << 32) | (52020305L & 0xffffffffL)),
            ((73056135L << 32) | (19508150L & 0xffffffffL)),
            ((528225046L << 32) | (125377439L & 0xffffffffL)),
            ((375490713L << 32) | (88700024L & 0xffffffffL)),
            ((440632116L << 32) | (100989606L & 0xffffffffL)),
            ((324933819L << 32) | (70554945L & 0xffffffffL)),
            ((142128170L << 32) | (40117608L & 0xffffffffL)),
            ((23408549L << 32) | (5524111L & 0xffffffffL)),
            ((179497824L << 32) | (46968951L & 0xffffffffL)),
            ((61829359L << 32) | (16572304L & 0xffffffffL)),
            ((411688062L << 32) | (111964089L & 0xffffffffL)),
            ((294930417L << 32) | (77332574L & 0xffffffffL)),
            ((498486364L << 32) | (131174016L & 0xffffffffL)),
            ((346806227L << 32) | (98689383L & 0xffffffffL)),
            ((260930370L << 32) | (61952334L & 0xffffffffL)),
            ((110158029L << 32) | (25247401L & 0xffffffffL)),
            ((399076376L << 32) | (86658290L & 0xffffffffL)),
            ((517273495L << 32) | (119153429L & 0xffffffffL)),
            ((99769094L << 32) | (21664572L & 0xffffffffL)),
            ((217038985L << 32) | (58391771L & 0xffffffffL)),
            ((13486884L << 32) | (3633669L & 0xffffffffL)),
            ((164647083L << 32) | (34020834L & 0xffffffffL)),
            ((318176314L << 32) | (72854987L & 0xffffffffL)),
            ((468412341L << 32) | (107463212L & 0xffffffffL)),
            ((53947536L << 32) | (14534678L & 0xffffffffL)),
            ((170571551L << 32) | (49129457L & 0xffffffffL)),
            ((286006158L << 32) | (79501272L & 0xffffffffL)),
            ((403800065L << 32) | (109934655L & 0xffffffffL)),
            ((338922412L << 32) | (96532193L & 0xffffffffL)),
            ((489558051L << 32) | (133208326L & 0xffffffffL)),
            ((101235890L << 32) | (27273519L & 0xffffffffL)),
            ((253044541L << 32) | (59786952L & 0xffffffffL)),
            ((508341224L << 32) | (121320595L & 0xffffffffL)),
            ((391196775L << 32) | (84630388L & 0xffffffffL)),
            ((209165558L << 32) | (56355677L & 0xffffffffL)),
            ((90834809L << 32) | (23823546L & 0xffffffffL)),
            ((155716820L << 32) | (36047460L & 0xffffffffL)),
            ((5609307L << 32) | (1467779L & 0xffffffffL)),
            ((460536778L << 32) | (105305514L & 0xffffffffL)),
            ((309239877L << 32) | (74889805L & 0xffffffffL)),
            ((215790144L << 32) | (58138714L & 0xffffffffL)),
            ((96545231L << 32) | (21450685L & 0xffffffffL)),
            ((518489438L << 32) | (119013268L & 0xffffffffL)),
            ((402267857L << 32) | (86478963L & 0xffffffffL)),
            ((467146108L << 32) | (107617965L & 0xffffffffL)),
            ((314937075L << 32) | (73036106L & 0xffffffffL)),
            ((165880418L << 32) | (34259299L & 0xffffffffL)),
            ((16693741L << 32) | (3845764L & 0xffffffffL)),
            ((296170808L << 32) | (76668127L & 0xffffffffL)),
            ((414887607L << 32) | (111260472L & 0xffffffffL)),
            ((60555814L << 32) | (15794961L & 0xffffffffL)),
            ((176265641L << 32) | (46230774L & 0xffffffffL)),
            ((111383044L << 32) | (26010152L & 0xffffffffL)),
            ((264112523L << 32) | (62688719L & 0xffffffffL)),
            ((345548058L << 32) | (99368422L & 0xffffffffL)),
            ((495271573L << 32) | (131879425L & 0xffffffffL)),
            ((89748912L << 32) | (23484475L & 0xffffffffL)),
            ((205844031L << 32) | (55982044L & 0xffffffffL)),
            ((392315566L << 32) | (84314101L & 0xffffffffL)),
            ((511695137L << 32) | (121038866L & 0xffffffffL)),
            ((308138636L << 32) | (75196108L & 0xffffffffL)),
            ((457197827L << 32) | (105580843L & 0xffffffffL)),
            ((6743442L << 32) | (1816834L & 0xffffffffL)),
            ((159088157L << 32) | (36427493L & 0xffffffffL)),
            ((404943560L << 32) | (109094078L & 0xffffffffL)),
            ((289368391L << 32) | (78695257L & 0xffffffffL)),
            ((169461206L << 32) | (48266096L & 0xffffffffL)),
            ((50617945L << 32) | (13636759L & 0xffffffffL)),
            ((254170612L << 32) | (60660297L & 0xffffffffL)),
            ((104582779L << 32) | (28177838L & 0xffffffffL)),
            ((488465130L << 32) | (134038919L & 0xffffffffL)),
            ((335608165L << 32) | (97331808L & 0xffffffffL)),
            ((534199712L << 32) | (123064472L & 0xffffffffL)),
            ((382379567L << 32) | (90554239L & 0xffffffffL)),
            ((233573054L << 32) | (53808982L & 0xffffffffL)),
            ((82940209L << 32) | (17129649L & 0xffffffffL)),
            ((148085404L << 32) | (38334063L & 0xffffffffL)),
            ((30277907L << 32) | (7897480L & 0xffffffffL)),
            ((451456386L << 32) | (103297441L & 0xffffffffL)),
            ((334837261L << 32) | (68705862L & 0xffffffffL)),
            ((44874456L << 32) | (11742237L & 0xffffffffL)),
            ((196157783L << 32) | (42157050L & 0xffffffffL)),
            ((278367686L << 32) | (80999379L & 0xffffffffL)),
            ((428480073L << 32) | (115579956L & 0xffffffffL)),
            ((363339236L << 32) | (95294186L & 0xffffffffL)),
            ((481658475L << 32) | (127827213L & 0xffffffffL)),
            ((127085306L << 32) | (30330148L & 0xffffffffL)),
            ((244232565L << 32) | (67019459L & 0xffffffffL)),
            ((374536784L << 32) | (88385785L & 0xffffffffL)),
            ((525296095L << 32) | (125093662L & 0xffffffffL)),
            ((74042702L << 32) | (19167031L & 0xffffffffL)),
            ((225728193L << 32) | (51648720L & 0xffffffffL)),
            ((22437228L << 32) | (5871118L & 0xffffffffL)),
            ((139183843L << 32) | (40499689L & 0xffffffffL)),
            ((325937778L << 32) | (70863296L & 0xffffffffL)),
            ((443609597L << 32) | (101262887L & 0xffffffffL)),
            ((187268392L << 32) | (44192892L & 0xffffffffL)),
            ((37021351L << 32) | (9583515L & 0xffffffffL)),
            ((420629046L << 32) | (113413042L & 0xffffffffL)),
            ((269472185L << 32) | (83027029L & 0xffffffffL)),
            ((472766996L << 32) | (129985163L & 0xffffffffL)),
            ((355484059L << 32) | (93259116L & 0xffffffffL)),
            ((236383498L << 32) | (64992581L & 0xffffffffL)),
            ((118191749L << 32) | (32496290L & 0xffffffffL))
    };
    private static final long Polynomial72[] = {((0L << 32) | (0L & 0xffffffffL)),
            ((335293334L << 32) | (-1961202135L & 0xffffffffL)),
            ((344628781L << 32) | (468213049L & 0xffffffffL)),
            ((125220283L << 32) | (-1863175408L & 0xffffffffL)),
            ((443020634L << 32) | (973880089L & 0xffffffffL)),
            ((161210060L << 32) | (-1323936464L & 0xffffffffL)),
            ((250440567L << 32) | (568616480L & 0xffffffffL)),
            ((487669985L << 32) | (-1426192375L & 0xffffffffL)),
            ((129835956L << 32) | (2042951513L & 0xffffffffL)),
            ((339806242L << 32) | (-220292752L & 0xffffffffL)),
            ((322420121L << 32) | (1647094368L & 0xffffffffL)),
            ((13342223L << 32) | (-382286775L & 0xffffffffL)),
            ((500881134L << 32) | (1137232960L & 0xffffffffL)),
            ((237436280L << 32) | (-925719959L & 0xffffffffL)),
            ((156256451L << 32) | (1478529401L & 0xffffffffL)),
            ((447505237L << 32) | (-751108272L & 0xffffffffL)),
            ((259671912L << 32) | (-209064270L & 0xffffffffL)),
            ((478571774L << 32) | (2022932635L & 0xffffffffL)),
            ((468769093L << 32) | (-396250229L & 0xffffffffL)),
            ((135066323L << 32) | (1669062050L & 0xffffffffL)),
            ((354267698L << 32) | (-913975893L & 0xffffffffL)),
            ((115448228L << 32) | (1117764482L & 0xffffffffL)),
            ((26684447L << 32) | (-764573550L & 0xffffffffL)),
            ((309004169L << 32) | (1501030075L & 0xffffffffL)),
            ((147285212L << 32) | (-1974658581L & 0xffffffffL)),
            ((456871754L << 32) | (22509506L & 0xffffffffL)),
            ((474872561L << 32) | (-1851439918L & 0xffffffffL)),
            ((263311719L << 32) | (448735995L & 0xffffffffL)),
            ((312512902L << 32) | (-1337908494L & 0xffffffffL)),
            ((22854160L << 32) | (995839195L & 0xffffffffL)),
            ((103419819L << 32) | (-1414955061L & 0xffffffffL)),
            ((366355517L << 32) | (548606434L & 0xffffffffL)),
            ((519343825L << 32) | (-418128540L & 0xffffffffL)),
            ((218670407L << 32) | (1812575053L & 0xffffffffL)),
            ((176034044L << 32) | (-50626467L & 0xffffffffL)),
            ((427965290L << 32) | (2011295348L & 0xffffffffL)),
            ((76774283L << 32) | (-585111939L & 0xffffffffL)),
            ((393170973L << 32) | (1443203156L & 0xffffffffL)),
            ((270132646L << 32) | (-956843196L & 0xffffffffL)),
            ((65392176L << 32) | (1307432301L & 0xffffffffL)),
            ((424258917L << 32) | (-1630066115L & 0xffffffffL)),
            ((179668723L << 32) | (365773844L & 0xffffffffL)),
            ((230896456L << 32) | (-2059438332L & 0xffffffffL)),
            ((507451614L << 32) | (237312301L & 0xffffffffL)),
            ((53368895L << 32) | (-1529147100L & 0xffffffffL)),
            ((282227625L << 32) | (801210125L & 0xffffffffL)),
            ((396674578L << 32) | (-1087157219L & 0xffffffffL)),
            ((72936836L << 32) | (875110964L & 0xffffffffL)),
            ((294570425L << 32) | (345650134L & 0xffffffffL)),
            ((41083439L << 32) | (-1618977281L & 0xffffffffL)),
            ((84170644L << 32) | (259157743L & 0xffffffffL)),
            ((385383426L << 32) | (-2073558842L & 0xffffffffL)),
            ((199834851L << 32) | (781584591L & 0xffffffffL)),
            ((404035445L << 32) | (-1517525274L & 0xffffffffL)),
            ((526623438L << 32) | (897471990L & 0xffffffffL)),
            ((211781976L << 32) | (-1100727329L & 0xffffffffL)),
            ((372501005L << 32) | (1834927247L & 0xffffffffL)),
            ((97501595L << 32) | (-431707482L & 0xffffffffL)),
            ((45708320L << 32) | (1991678390L & 0xffffffffL)),
            ((289759158L << 32) | (-38996065L & 0xffffffffL)),
            ((206839639L << 32) | (1465057174L & 0xffffffffL)),
            ((531117249L << 32) | (-599223873L & 0xffffffffL)),
            ((417235322L << 32) | (1287299759L & 0xffffffffL)),
            ((186821356L << 32) | (-945763194L & 0xffffffffL)),
            ((245093539L << 32) | (-1006945373L & 0xffffffffL)),
            ((493354805L << 32) | (1222727050L & 0xffffffffL)),
            ((437340814L << 32) | (-669817190L & 0xffffffffL)),
            ((166551832L << 32) | (1393100979L & 0xffffffffL)),
            ((352068089L << 32) | (-101252934L & 0xffffffffL)),
            ((117443183L << 32) | (1928162963L & 0xffffffffL)),
            ((7782356L << 32) | (-501260925L & 0xffffffffL)),
            ((327849026L << 32) | (1761948586L & 0xffffffffL)),
            ((153548567L << 32) | (-1170223878L & 0xffffffffL)),
            ((450550913L << 32) | (824550099L & 0xffffffffL)),
            ((497838394L << 32) | (-1579707965L & 0xffffffffL)),
            ((240140972L << 32) | (718143466L & 0xffffffffL)),
            ((323027533L << 32) | (-2144077853L & 0xffffffffL)),
            ((12397019L << 32) | (187275722L & 0xffffffffL)),
            ((130784352L << 32) | (-1680102694L & 0xffffffffL)),
            ((339195894L << 32) | (281134323L & 0xffffffffL)),
            ((31560651L << 32) | (812832017L & 0xffffffffL)),
            ((303937629L << 32) | (-1150764232L & 0xffffffffL)),
            ((359337446L << 32) | (731547688L & 0xffffffffL)),
            ((110569072L << 32) | (-1602165247L & 0xffffffffL)),
            ((461792913L << 32) | (176090632L & 0xffffffffL)),
            ((142232839L << 32) | (-2124120031L & 0xffffffffL)),
            ((252508348L << 32) | (295088945L & 0xffffffffL)),
            ((485544746L << 32) | (-1702044392L & 0xffffffffL)),
            ((106737791L << 32) | (1236673096L & 0xffffffffL)),
            ((362847209L << 32) | (-1028895647L & 0xffffffffL)),
            ((316026450L << 32) | (1381924721L & 0xffffffffL)),
            ((19531204L << 32) | (-649850536L & 0xffffffffL)),
            ((473646373L << 32) | (1941576017L & 0xffffffffL)),
            ((264728243L << 32) | (-123701384L & 0xffffffffL)),
            ((145873672L << 32) | (1750221928L & 0xffffffffL)),
            ((458092702L << 32) | (-481809855L & 0xffffffffL)),
            ((275733106L << 32) | (619243207L & 0xffffffffL)),
            ((59994596L << 32) | (-1343059730L & 0xffffffffL)),
            ((82166879L << 32) | (1057012734L & 0xffffffffL)),
            ((387575753L << 32) | (-1273309737L & 0xffffffffL)),
            ((168341288L << 32) | (518315486L & 0xffffffffL)),
            ((435455166L << 32) | (-1778469897L & 0xffffffffL)),
            ((511848709L << 32) | (84705511L & 0xffffffffL)),
            ((226368147L << 32) | (-1911099698L & 0xffffffffL)),
            ((399669702L << 32) | (1563169182L & 0xffffffffL)),
            ((70144592L << 32) | (-701071433L & 0xffffffffL)),
            ((56158187L << 32) | (1187269799L & 0xffffffffL)),
            ((279235709L << 32) | (-841080178L & 0xffffffffL)),
            ((230001820L << 32) | (1730161287L & 0xffffffffL)),
            ((508143370L << 32) | (-331725650L & 0xffffffffL)),
            ((423563953L << 32) | (2093512638L & 0xffffffffL)),
            ((180566311L << 32) | (-137225833L & 0xffffffffL)),
            ((521477402L << 32) | (-681506699L & 0xffffffffL)),
            ((216610444L << 32) | (1551591004L & 0xffffffffL)),
            ((195003191L << 32) | (-863414964L & 0xffffffffL)),
            ((409184417L << 32) | (1200831333L & 0xffffffffL)),
            ((91416640L << 32) | (-311610516L & 0xffffffffL)),
            ((378454998L << 32) | (1719098693L & 0xffffffffL)),
            ((301495917L << 32) | (-159027627L & 0xffffffffL)),
            ((33840635L << 32) | (2107572348L & 0xffffffffL)),
            ((413679278L << 32) | (-1364852948L & 0xffffffffL)),
            ((190059832L << 32) | (633311493L & 0xffffffffL)),
            ((203595907L << 32) | (-1253203435L & 0xffffffffL)),
            ((534678293L << 32) | (1045941308L & 0xffffffffL)),
            ((47172596L << 32) | (-1800813515L & 0xffffffffL)),
            ((288612450L << 32) | (531868188L & 0xffffffffL)),
            ((373642713L << 32) | (-1891526388L & 0xffffffffL)),
            ((96042575L << 32) | (73135909L & 0xffffffffL)),
            ((490187079L << 32) | (-2013890746L & 0xffffffffL)),
            ((248218321L << 32) | (216799599L & 0xffffffffL)),
            ((163395434L << 32) | (-1675744641L & 0xffffffffL)),
            ((440474876L << 32) | (386155606L & 0xffffffffL)),
            ((122706973L << 32) | (-1107669921L & 0xffffffffL)),
            ((346847115L << 32) | (920658550L & 0xffffffffL)),
            ((333103664L << 32) | (-1508765338L & 0xffffffffL)),
            ((2550182L << 32) | (755531599L & 0xffffffffL)),
            ((445287155L << 32) | (-30244833L & 0xffffffffL)),
            ((158769509L << 32) | (1965616694L & 0xffffffffL)),
            ((234886366L << 32) | (-438641370L & 0xffffffffL)),
            ((503070536L << 32) | (1858122511L & 0xffffffffL)),
            ((15564713L << 32) | (-1002521850L & 0xffffffffL)),
            ((319902783L << 32) | (1327813935L & 0xffffffffL)),
            ((342352260L << 32) | (-539564481L & 0xffffffffL)),
            ((127650322L << 32) | (1422690326L & 0xffffffffL)),
            ((307097135L << 32) | (1954519540L & 0xffffffffL)),
            ((28427705L << 32) | (-10094627L & 0xffffffffL)),
            ((113733634L << 32) | (1872217293L & 0xffffffffL)),
            ((356211604L << 32) | (-460477724L & 0xffffffffL)),
            ((136977269L << 32) | (1316201197L & 0xffffffffL)),
            ((467022051L << 32) | (-982922044L & 0xffffffffL)),
            ((480281944L << 32) | (1436286932L & 0xffffffffL)),
            ((257732302L << 32) | (-561933827L & 0xffffffffL)),
            ((368102811L << 32) | (230387373L & 0xffffffffL)),
            ((101508621L << 32) | (-2036268924L & 0xffffffffL)),
            ((24794038L << 32) | (374551444L & 0xffffffffL)),
            ((310802464L << 32) | (-1656136259L & 0xffffffffL)),
            ((261568705L << 32) | (934761908L & 0xffffffffL)),
            ((476779351L << 32) | (-1129497699L & 0xffffffffL)),
            ((454928108L << 32) | (744425613L & 0xffffffffL)),
            ((148999546L << 32) | (-1488623964L & 0xffffffffL)),
            ((63121302L << 32) | (1625664034L & 0xffffffffL)),
            ((272567296L << 32) | (-335559669L & 0xffffffffL)),
            ((390707643L << 32) | (2064512795L & 0xffffffffL)),
            ((79008301L << 32) | (-266888910L & 0xffffffffL)),
            ((430232268L << 32) | (1525256507L & 0xffffffffL)),
            ((173603162L << 32) | (-772538606L & 0xffffffffL)),
            ((221138145L << 32) | (1090636802L & 0xffffffffL)),
            ((517105527L << 32) | (-904158677L & 0xffffffffL)),
            ((75367458L << 32) | (421617019L & 0xffffffffL)),
            ((394407860L << 32) | (-1841613998L & 0xffffffffL)),
            ((284465679L << 32) | (46727234L & 0xffffffffL)),
            ((50901401L << 32) | (-1982632341L & 0xffffffffL)),
            ((505016696L << 32) | (590177890L & 0xffffffffL)),
            ((233167598L << 32) | (-1472788405L & 0xffffffffL)),
            ((177434453L << 32) | (952449883L & 0xffffffffL)),
            ((426722499L << 32) | (-1277209230L & 0xffffffffL)),
            ((213475582L << 32) | (-1821621104L & 0xffffffffL)),
            ((524634984L << 32) | (410397369L & 0xffffffffL)),
            ((406060755L << 32) | (-2004608599L & 0xffffffffL)),
            ((198169925L << 32) | (60716928L & 0xffffffffL)),
            ((383686052L << 32) | (-1453293687L & 0xffffffffL)),
            ((86162994L << 32) | (578425248L & 0xffffffffL)),
            ((39062409L << 32) | (-1299701072L & 0xffffffffL)),
            ((296230943L << 32) | (965889177L & 0xffffffffL)),
            ((184828746L << 32) | (-358042679L & 0xffffffffL)),
            ((418932956L << 32) | (1639112160L & 0xffffffffL)),
            ((529456487L << 32) | (-247402768L & 0xffffffffL)),
            ((208860913L << 32) | (2052751577L & 0xffffffffL)),
            ((291747344L << 32) | (-794523440L & 0xffffffffL)),
            ((44014982L << 32) | (1539237625L & 0xffffffffL)),
            ((99166269L << 32) | (-884156951L & 0xffffffffL)),
            ((370475947L << 32) | (1079425984L & 0xffffffffL)),
            ((330071524L << 32) | (1141722341L & 0xffffffffL)),
            ((5265010L << 32) | (-820567348L & 0xffffffffL)),
            ((119989193L << 32) | (1608847836L & 0xffffffffL)),
            ((349882463L << 32) | (-721453067L & 0xffffffffL)),
            ((164333758L << 32) | (2114025468L & 0xffffffffL)),
            ((439853864L << 32) | (-182773291L & 0xffffffffL)),
            ((490804883L << 32) | (1709779653L & 0xffffffffL)),
            ((247282949L << 32) | (-286046996L & 0xffffffffL)),
            ((336682576L << 32) | (1036630972L & 0xffffffffL)),
            ((133002694L << 32) | (-1227631211L & 0xffffffffL)),
            ((10207357L << 32) | (639755909L & 0xffffffffL)),
            ((325577707L << 32) | (-1388607316L & 0xffffffffL)),
            ((242658058L << 32) | (130384037L & 0xffffffffL)),
            ((495616156L << 32) | (-1931481460L & 0xffffffffL)),
            ((452736295L << 32) | (472767900L & 0xffffffffL)),
            ((151002801L << 32) | (-1757957195L & 0xffffffffL)),
            ((483801740L << 32) | (-1216044457L & 0xffffffffL)),
            ((254415130L << 32) | (1017039998L & 0xffffffffL)),
            ((140289185L << 32) | (-1402142866L & 0xffffffffL)),
            ((463507255L << 32) | (662081863L & 0xffffffffL)),
            ((112316374L << 32) | (-1920427698L & 0xffffffffL)),
            ((357426240L << 32) | (110294887L & 0xffffffffL)),
            ((305877499L << 32) | (-1772043145L & 0xffffffffL)),
            ((29850221L << 32) | (494578270L & 0xffffffffL)),
            ((460003640L << 32) | (-834644722L & 0xffffffffL)),
            ((144126638L << 32) | (1163541287L & 0xffffffffL)),
            ((266438421L << 32) | (-710408137L & 0xffffffffL)),
            ((471706755L << 32) | (1588749854L & 0xffffffffL)),
            ((17624162L << 32) | (-196317673L & 0xffffffffL)),
            ((317769716L << 32) | (2136342590L & 0xffffffffL)),
            ((361132623L << 32) | (-274451666L & 0xffffffffL)),
            ((108681689L << 32) | (1690197255L & 0xffffffffL)),
            ((223933237L << 32) | (-1558277759L & 0xffffffffL)),
            ((514119843L << 32) | (671416232L & 0xffffffffL)),
            ((433220888L << 32) | (-1191785288L & 0xffffffffL)),
            ((170804878L << 32) | (871146129L & 0xffffffffL)),
            ((390006383L << 32) | (-1726829928L & 0xffffffffL)),
            ((79900153L << 32) | (302564529L & 0xffffffffL)),
            ((62232642L << 32) | (-2097481823L & 0xffffffffL)),
            ((273265620L << 32) | (165714312L & 0xffffffffL)),
            ((182833281L << 32) | (-623221032L & 0xffffffffL)),
            ((421133079L << 32) | (1371539697L & 0xffffffffL)),
            ((510611116L << 32) | (-1053672479L & 0xffffffffL)),
            ((227763514L << 32) | (1244157384L & 0xffffffffL)),
            ((276964827L << 32) | (-522822207L & 0xffffffffL)),
            ((58592845L << 32) | (1808544744L & 0xffffffffL)),
            ((67681270L << 32) | (-79822600L & 0xffffffffL)),
            ((401903712L << 32) | (1881435857L & 0xffffffffL)),
            ((35828829L << 32) | (1352105779L & 0xffffffffL)),
            ((299802571L << 32) | (-611512038L & 0xffffffffL)),
            ((380119664L << 32) | (1266622986L & 0xffffffffL)),
            ((89391590L << 32) | (-1067103197L & 0xffffffffL)),
            ((407191815L << 32) | (1788560426L & 0xffffffffL)),
            ((196700817L << 32) | (-511628797L & 0xffffffffL)),
            ((214949674L << 32) | (1903368467L & 0xffffffffL)),
            ((523498684L << 32) | (-93751494L & 0xffffffffL)),
            ((94345193L << 32) | (693340266L & 0xffffffffL)),
            ((375635071L << 32) | (-1572215229L & 0xffffffffL)),
            ((286591428L << 32) | (851170643L & 0xffffffffL)),
            ((48833106L << 32) | (-1180583046L & 0xffffffffL)),
            ((536371891L << 32) | (325038963L & 0xffffffffL)),
            ((201607461L << 32) | (-1740251814L & 0xffffffffL)),
            ((192085150L << 32) | (146271818L & 0xffffffffL)),
            ((412014344L << 32) | (-2085781405L & 0xffffffffL))
    };
    private static final long Polynomial80[] = {((0L << 32) | (0L & 0xffffffffL)),
            ((125726524L << 32) | (-1753253426L & 0xffffffffL)),
            ((251453049L << 32) | (788460444L & 0xffffffffL)),
            ((159560005L << 32) | (-1182692782L & 0xffffffffL)),
            ((502906098L << 32) | (1576920888L & 0xffffffffL)),
            ((445109198L << 32) | (-897409290L & 0xffffffffL)),
            ((319120011L << 32) | (1929581732L & 0xffffffffL)),
            ((343608759L << 32) | (-461607574L & 0xffffffffL)),
            ((142717156L << 32) | (-1238937829L & 0xffffffffL)),
            ((268427224L << 32) | (559429333L & 0xffffffffL)),
            ((108883613L << 32) | (-1730560889L & 0xffffffffL)),
            ((16974241L << 32) | (262587721L & 0xffffffffL)),
            ((360188950L << 32) | (-337991645L & 0xffffffffL)),
            ((302408490L << 32) | (2091241965L & 0xffffffffL)),
            ((461689455L << 32) | (-987472961L & 0xffffffffL)),
            ((486194515L << 32) | (1381704305L & 0xffffffffL)),
            ((285434313L << 32) | (1817091638L & 0xffffffffL)),
            ((377294581L << 32) | (-80615432L & 0xffffffffL)),
            ((536854448L << 32) | (1118858666L & 0xffffffffL)),
            ((411160716L << 32) | (-707849116L & 0xffffffffL)),
            ((217767227L << 32) | (833845518L & 0xffffffffL)),
            ((193245703L << 32) | (-1496579904L & 0xffffffffL)),
            ((33948482L << 32) | (525175442L & 0xffffffffL)),
            ((91778174L << 32) | (-2009926820L & 0xffffffffL)),
            ((428003629L << 32) | (-630599379L & 0xffffffffL)),
            ((519880209L << 32) | (1293330659L & 0xffffffffL)),
            ((394137428L << 32) | (-191421775L & 0xffffffffL)),
            ((268460136L << 32) | (1676172159L & 0xffffffffL)),
            ((75197919L << 32) | (-2020329963L & 0xffffffffL)),
            ((50660067L << 32) | (283856859L & 0xffffffffL)),
            ((176665510L << 32) | (-1452620407L & 0xffffffffL)),
            ((234478746L << 32) | (1041611847L & 0xffffffffL)),
            ((292817554L << 32) | (-717112057L & 0xffffffffL)),
            ((369780142L << 32) | (1111409865L & 0xffffffffL)),
            ((529061099L << 32) | (-71348581L & 0xffffffffL)),
            ((418823127L << 32) | (1824536405L & 0xffffffffL)),
            ((210613856L << 32) | (-2000922049L & 0xffffffffL)),
            ((200530268L << 32) | (532882417L & 0xffffffffL)),
            ((40986649L << 32) | (-1505580637L & 0xffffffffL)),
            ((84870949L << 32) | (826134637L & 0xffffffffL)),
            ((435534454L << 32) | (1667691036L & 0xffffffffL)),
            ((512480586L << 32) | (-199652398L & 0xffffffffL)),
            ((386491407L << 32) | (1301807488L & 0xffffffffL)),
            ((276237107L << 32) | (-622364594L & 0xffffffffL)),
            ((67896964L << 32) | (1050350884L & 0xffffffffL)),
            ((57829816L << 32) | (-1444647702L & 0xffffffffL)),
            ((183556349L << 32) | (275113656L & 0xffffffffL)),
            ((227456961L << 32) | (-2028298378L & 0xffffffffL)),
            ((7826267L << 32) | (-1190190287L & 0xffffffffL)),
            ((118031463L << 32) | (779115263L & 0xffffffffL)),
            ((244036898L << 32) | (-1745760083L & 0xffffffffL)),
            ((167107102L << 32) | (9349475L & 0xffffffffL)),
            ((495900585L << 32) | (-453852151L & 0xffffffffL)),
            ((451983509L << 32) | (1938668999L & 0xffffffffL)),
            ((326240720L << 32) | (-905169003L & 0xffffffffL)),
            ((336357100L << 32) | (1567837787L & 0xffffffffL)),
            ((150395839L << 32) | (254307370L & 0xffffffffL)),
            ((260617347L << 32) | (-1739123228L & 0xffffffffL)),
            ((101320134L << 32) | (567713718L & 0xffffffffL)),
            ((24406778L << 32) | (-1230379400L & 0xffffffffL)),
            ((353331021L << 32) | (1389726482L & 0xffffffffL)),
            ((309397617L << 32) | (-978652452L & 0xffffffffL)),
            ((468957492L << 32) | (2083223694L & 0xffffffffL)),
            ((479057416L << 32) | (-346816192L & 0xffffffffL)),
            ((295361573L << 32) | (-1486881947L & 0xffffffffL)),
            ((384086809L << 32) | (807436971L & 0xffffffffL)),
            ((526778972L << 32) | (-1985935111L & 0xffffffffL)),
            ((404254048L << 32) | (517894455L & 0xffffffffL)),
            ((207839447L << 32) | (-90039203L & 0xffffffffL)),
            ((186453995L << 32) | (1843226003L & 0xffffffffL)),
            ((44023470L << 32) | (-732106815L & 0xffffffffL)),
            ((98685330L << 32) | (1126405647L & 0xffffffffL)),
            ((421227713L << 32) | (293123198L & 0xffffffffL)),
            ((509936637L << 32) | (-2046308944L & 0xffffffffL)),
            ((401060536L << 32) | (1065764834L & 0xffffffffL)),
            ((278519172L << 32) | (-1460060628L & 0xffffffffL)),
            ((81973299L << 32) | (1283806022L & 0xffffffffL)),
            ((60604175L << 32) | (-604362104L & 0xffffffffL)),
            ((169741898L << 32) | (1652269274L & 0xffffffffL)),
            ((224420214L << 32) | (-184231660L & 0xffffffffL)),
            ((10075628L << 32) | (-888052397L & 0xffffffffL)),
            ((132633296L << 32) | (1550720157L & 0xffffffffL)),
            ((241525653L << 32) | (-437283121L & 0xffffffffL)),
            ((152767657L << 32) | (1922100993L & 0xffffffffL)),
            ((492831006L << 32) | (-1762885013L & 0xffffffffL)),
            ((438201890L << 32) | (26475429L & 0xffffffffL)),
            ((329047911L << 32) | (-1206750729L & 0xffffffffL)),
            ((350400603L << 32) | (795674681L & 0xffffffffL)),
            ((135793928L << 32) | (2100701768L & 0xffffffffL)),
            ((258368052L << 32) | (-364293242L & 0xffffffffL)),
            ((115659633L << 32) | (1405671892L & 0xffffffffL)),
            ((26917965L << 32) | (-994598886L & 0xffffffffL)),
            ((367112698L << 32) | (550227312L & 0xffffffffL)),
            ((312467142L << 32) | (-1212894018L & 0xffffffffL)),
            ((454913923L << 32) | (238370540L & 0xffffffffL)),
            ((476250303L << 32) | (-1723185374L & 0xffffffffL)),
            ((15652535L << 32) | (1914586722L & 0xffffffffL)),
            ((126925195L << 32) | (-446611540L & 0xffffffffL)),
            ((236062926L << 32) | (1558230526L & 0xffffffffL)),
            ((158099442L << 32) | (-878719952L & 0xffffffffL)),
            ((488073797L << 32) | (803447130L & 0xffffffffL)),
            ((443090297L << 32) | (-1197680492L & 0xffffffffL)),
            ((334214204L << 32) | (18698950L & 0xffffffffL)),
            ((345365248L << 32) | (-1771951352L & 0xffffffffL)),
            ((141518419L << 32) | (-1002894983L & 0xffffffffL)),
            ((252774767L << 32) | (1397125303L & 0xffffffffL)),
            ((110344234L << 32) | (-355992859L & 0xffffffffL)),
            ((32364310L << 32) | (2109244203L & 0xffffffffL)),
            ((362207905L << 32) | (-1715147199L & 0xffffffffL)),
            ((317240733L << 32) | (247175055L & 0xffffffffL)),
            ((459932888L << 32) | (-1220928035L & 0xffffffffL)),
            ((471100388L << 32) | (541418515L & 0xffffffffL)),
            ((300791678L << 32) | (508614740L & 0xffffffffL)),
            ((378787906L << 32) | (-1993367142L & 0xffffffffL)),
            ((521234695L << 32) | (816720840L & 0xffffffffL)),
            ((409929275L << 32) | (-1479454202L & 0xffffffffL)),
            ((202640268L << 32) | (1135427436L & 0xffffffffL)),
            ((191521968L << 32) | (-724416862L & 0xffffffffL)),
            ((48813557L << 32) | (1834208496L & 0xffffffffL)),
            ((93764297L << 32) | (-97733314L & 0xffffffffL)),
            ((426510234L << 32) | (-1468557489L & 0xffffffffL)),
            ((504522918L << 32) | (1057549953L & 0xffffffffL)),
            ((395368931L << 32) | (-2037816109L & 0xffffffffL)),
            ((284079839L << 32) | (301341981L & 0xffffffffL)),
            ((76921704L << 32) | (-175476617L & 0xffffffffL)),
            ((65786964L << 32) | (1660225977L & 0xffffffffL)),
            ((174679313L << 32) | (-613121045L & 0xffffffffL)),
            ((219613741L << 32) | (1275853349L & 0xffffffffL)),
            ((273117515L << 32) | (1130592161L & 0xffffffffL)),
            ((389619319L << 32) | (-736358801L & 0xffffffffL)),
            ((515616562L << 32) | (1839039549L & 0xffffffffL)),
            ((432390158L << 32) | (-85787149L & 0xffffffffL)),
            ((230608313L << 32) | (513708185L & 0xffffffffL)),
            ((180396677L << 32) | (-1981683369L & 0xffffffffL)),
            ((54662080L << 32) | (811623173L & 0xffffffffL)),
            ((71073020L << 32) | (-1491133749L & 0xffffffffL)),
            ((415678895L << 32) | (-180078406L & 0xffffffffL)),
            ((532197011L << 32) | (1648050548L & 0xffffffffL)),
            ((372907990L << 32) | (-608515290L & 0xffffffffL)),
            ((289698026L << 32) | (1288024808L & 0xffffffffL)),
            ((88046941L << 32) | (-1464213630L & 0xffffffffL)),
            ((37818977L << 32) | (1069983308L & 0xffffffffL)),
            ((197370660L << 32) | (-2042156002L & 0xffffffffL)),
            ((213765144L << 32) | (288904656L & 0xffffffffL)),
            ((21237890L << 32) | (791538071L & 0xffffffffL)),
            ((104497086L << 32) | (-1202548647L & 0xffffffffL)),
            ((263769851L << 32) | (30611979L & 0xffffffffL)),
            ((147235271L << 32) | (-1767087163L & 0xffffffffL)),
            ((482192496L << 32) | (1926237871L & 0xffffffffL)),
            ((465814348L << 32) | (-441485471L & 0xffffffffL)),
            ((306278921L << 32) | (1546583347L & 0xffffffffL)),
            ((356457781L << 32) | (-883849987L & 0xffffffffL)),
            ((163946598L << 32) | (-1727355252L & 0xffffffffL)),
            ((247189338L << 32) | (242605890L & 0xffffffffL)),
            ((121208351L << 32) | (-1208724208L & 0xffffffffL)),
            ((4657443L << 32) | (545991902L & 0xffffffffL)),
            ((339483796L << 32) | (-990428748L & 0xffffffffL)),
            ((323122088L << 32) | (1401436282L & 0xffffffffL)),
            ((448840429L << 32) | (-368463320L & 0xffffffffL)),
            ((499035601L << 32) | (2104937446L & 0xffffffffL)),
            ((20151257L << 32) | (-1776104794L & 0xffffffffL)),
            ((105714917L << 32) | (22917992L & 0xffffffffL)),
            ((265266592L << 32) | (-1193526982L & 0xffffffffL)),
            ((145869468L << 32) | (799228148L & 0xffffffffL)),
            ((483051307L << 32) | (-874566242L & 0xffffffffL)),
            ((464824343L << 32) | (1554011216L & 0xffffffffL)),
            ((305535314L << 32) | (-450765310L & 0xffffffffL)),
            ((357070446L << 32) | (1918805964L & 0xffffffffL)),
            ((162712381L << 32) | (537232829L & 0xffffffffL)),
            ((248292353L << 32) | (-1216676749L & 0xffffffffL)),
            ((122557764L << 32) | (251360801L & 0xffffffffL)),
            ((3177080L << 32) | (-1719398417L & 0xffffffffL)),
            ((340490191L << 32) | (2113430149L & 0xffffffffL)),
            ((322246899L << 32) | (-360244405L & 0xffffffffL)),
            ((448244150L << 32) | (1392939289L & 0xffffffffL)),
            ((499762826L << 32) | (-998643497L & 0xffffffffL)),
            ((271587856L << 32) | (-93563760L & 0xffffffffL)),
            ((391017772L << 32) | (1829973342L & 0xffffffffL)),
            ((516736105L << 32) | (-728586484L & 0xffffffffL)),
            ((431139669L << 32) | (1139662530L & 0xffffffffL)),
            ((231319266L << 32) | (-1483623512L & 0xffffffffL)),
            ((179816926L << 32) | (820955750L & 0xffffffffL)),
            ((53835931L << 32) | (-1989197772L & 0xffffffffL)),
            ((72030119L << 32) | (504379898L & 0xffffffffL)),
            ((414296820L << 32) | (1279990667L & 0xffffffffL)),
            ((533710280L << 32) | (-617323963L & 0xffffffffL)),
            ((374174861L << 32) | (1656088599L & 0xffffffffL)),
            ((288562097L << 32) | (-171273767L & 0xffffffffL)),
            ((88610310L << 32) | (297204915L & 0xffffffffL)),
            ((37124410L << 32) | (-2033613443L & 0xffffffffL)),
            ((196397183L << 32) | (1061687087L & 0xffffffffL)),
            ((214607683L << 32) | (-1472760095L & 0xffffffffL)),
            ((31305070L << 32) | (-465793852L & 0xffffffffL)),
            ((111395410L << 32) | (1933833482L & 0xffffffffL)),
            ((253850391L << 32) | (-893223080L & 0xffffffffL)),
            ((140450859L << 32) | (1572669078L & 0xffffffffL)),
            ((472125852L << 32) | (-1178506244L & 0xffffffffL)),
            ((458915488L << 32) | (784208434L & 0xffffffffL)),
            ((316198885L << 32) | (-1757439904L & 0xffffffffL)),
            ((363241689L << 32) | (4252078L & 0xffffffffL)),
            ((157031818L << 32) | (1377551327L & 0xffffffffL)),
            ((237138614L << 32) | (-983254511L & 0xffffffffL)),
            ((127976435L << 32) | (2095394883L & 0xffffffffL)),
            ((14593231L << 32) | (-342210163L & 0xffffffffL)),
            ((346399096L << 32) | (266740967L & 0xffffffffL)),
            ((333172292L << 32) | (-1734779607L & 0xffffffffL)),
            ((442072833L << 32) | (555276155L & 0xffffffffL)),
            ((489099325L << 32) | (-1234719051L & 0xffffffffL)),
            ((283036839L << 32) | (-2005789966L & 0xffffffffL)),
            ((396403611L << 32) | (520973116L & 0xffffffffL)),
            ((505549534L << 32) | (-1500716690L & 0xffffffffL)),
            ((425491938L << 32) | (838047904L & 0xffffffffL)),
            ((220688469L << 32) | (-711985718L & 0xffffffffL)),
            ((173612905L << 32) | (1123060740L & 0xffffffffL)),
            ((64728620L << 32) | (-76478890L & 0xffffffffL)),
            ((77971728L << 32) | (1812889496L & 0xffffffffL)),
            ((408910915L << 32) | (1045781993L & 0xffffffffL)),
            ((522261375L << 32) | (-1456856025L & 0xffffffffL)),
            ((379822650L << 32) | (279686773L & 0xffffffffL)),
            ((299748614L << 32) | (-2016094277L & 0xffffffffL)),
            ((94814385L << 32) | (1672002257L & 0xffffffffL)),
            ((47755149L << 32) | (-187186401L & 0xffffffffL)),
            ((190455496L << 32) | (1297500493L & 0xffffffffL)),
            ((203715060L << 32) | (-634834813L & 0xffffffffL)),
            ((279554044L << 32) | (830288323L & 0xffffffffL)),
            ((400017600L << 32) | (-1509799923L & 0xffffffffL)),
            ((508918149L << 32) | (528728671L & 0xffffffffL)),
            ((422254265L << 32) | (-1996702831L & 0xffffffffL)),
            ((223353614L << 32) | (1820382971L & 0xffffffffL)),
            ((170816562L << 32) | (-67129547L & 0xffffffffL)),
            ((61654391L << 32) | (1115563367L & 0xffffffffL)),
            ((80915019L << 32) | (-721331031L & 0xffffffffL)),
            ((405280536L << 32) | (-2024112424L & 0xffffffffL)),
            ((525760548L << 32) | (270862102L & 0xffffffffL)),
            ((383043937L << 32) | (-1448833724L & 0xffffffffL)),
            ((296396381L << 32) | (1054602378L & 0xffffffffL)),
            ((97627114L << 32) | (-626550304L & 0xffffffffL)),
            ((45073622L << 32) | (1306058798L & 0xffffffffL)),
            ((187528595L << 32) | (-195466628L & 0xffffffffL)),
            ((206772911L << 32) | (1663439794L & 0xffffffffL)),
            ((27969077L << 32) | (1563668469L & 0xffffffffL)),
            ((114600201L << 32) | (-900934085L & 0xffffffffL)),
            ((257300556L << 32) | (1942838377L & 0xffffffffL)),
            ((136869744L << 32) | (-458087001L & 0xffffffffL)),
            ((475232967L << 32) | (13519053L & 0xffffffffL)),
            ((455939579L << 32) | (-1749995261L & 0xffffffffL)),
            ((313500862L << 32) | (774945617L & 0xffffffffL)),
            ((366070658L << 32) | (-1185955169L & 0xffffffffL)),
            ((153843409L << 32) | (-350953234L & 0xffffffffL)),
            ((240458221L << 32) | (2087426336L & 0xffffffffL)),
            ((131573928L << 32) | (-974515342L & 0xffffffffL)),
            ((11126676L << 32) | (1385523900L & 0xffffffffL)),
            ((349358627L << 32) | (-1226242090L & 0xffffffffL)),
            ((330081567L << 32) | (563510808L & 0xffffffffL)),
            ((439227482L << 32) | (-1743260598L & 0xffffffffL)),
            ((491813734L << 32) | (258510212L & 0xffffffffL))
    };
    private static final long Polynomial88[] = {((0L << 32) | (0L & 0xffffffffL)),
            ((346020725L << 32) | (964379295L & 0xffffffffL)),
            ((441286634L << 32) | (2133460053L & 0xffffffffL)),
            ((248685727L << 32) | (1179731658L & 0xffffffffL)),
            ((132658900L << 32) | (-209155647L & 0xffffffffL)),
            ((326626721L << 32) | (-889992354L & 0xffffffffL)),
            ((497371454L << 32) | (-1935503980L & 0xffffffffL)),
            ((154833483L << 32) | (-1244016885L & 0xffffffffL)),
            ((265317801L << 32) | (-418311294L & 0xffffffffL)),
            ((458208988L << 32) | (-563457763L & 0xffffffffL)),
            ((362615363L << 32) | (-1740957737L & 0xffffffffL)),
            ((16959798L << 32) | (-1589619384L & 0xffffffffL)),
            ((137911165L << 32) | (345610819L & 0xffffffffL)),
            ((480739336L << 32) | (769841372L & 0xffffffffL)),
            ((309666967L << 32) | (1806933526L & 0xffffffffL)),
            ((116064226L << 32) | (1388895369L & 0xffffffffL)),
            ((530635603L << 32) | (-836622588L & 0xffffffffL)),
            ((188678182L << 32) | (-145136229L & 0xffffffffL)),
            ((99460281L << 32) | (-1324866735L & 0xffffffffL)),
            ((292716492L << 32) | (-2005704242L & 0xffffffffL)),
            ((407432583L << 32) | (1034596037L & 0xffffffffL)),
            ((215430898L << 32) | (80866394L & 0xffffffffL)),
            ((33919597L << 32) | (1115728528L & 0xffffffffL)),
            ((379210008L << 32) | (2080106511L & 0xffffffffL)),
            ((275822330L << 32) | (691221638L & 0xffffffffL)),
            ((82800015L << 32) | (273184281L & 0xffffffffL)),
            ((171821328L << 32) | (1444491475L & 0xffffffffL)),
            ((513938021L << 32) | (1868722764L & 0xffffffffL)),
            ((395870254L << 32) | (-625230521L & 0xffffffffL)),
            ((50813787L << 32) | (-473890856L & 0xffffffffL)),
            ((232128452L << 32) | (-1517176558L & 0xffffffffL)),
            ((424289457L << 32) | (-1662321779L & 0xffffffffL)),
            ((204701607L << 32) | (-1852256413L & 0xffffffffL)),
            ((413967570L << 32) | (-1461481988L & 0xffffffffL)),
            ((377356365L << 32) | (-290272458L & 0xffffffffL)),
            ((48356152L << 32) | (-674657879L & 0xffffffffL)),
            ((198920563L << 32) | (1645233826L & 0xffffffffL)),
            ((524587526L << 32) | (1533740093L & 0xffffffffL)),
            ((295057049L << 32) | (490357495L & 0xffffffffL)),
            ((84536812L << 32) | (608239720L & 0xffffffffL)),
            ((65212942L << 32) | (1988746465L & 0xffffffffL)),
            ((394054011L << 32) | (1341300350L & 0xffffffffL)),
            ((430861796L << 32) | (161732788L & 0xffffffffL)),
            ((221361809L << 32) | (819501611L & 0xffffffffL)),
            ((67839194L << 32) | (-2063510240L & 0xffffffffL)),
            ((278200239L << 32) | (-1132849217L & 0xffffffffL)),
            ((507927344L << 32) | (-97824395L & 0xffffffffL)),
            ((182026309L << 32) | (-1018162198L & 0xffffffffL)),
            ((328443124L << 32) | (1606084711L & 0xffffffffL)),
            ((118259585L << 32) | (1723968248L & 0xffffffffL)),
            ((165600030L << 32) | (546368562L & 0xffffffffL)),
            ((490799211L << 32) | (434876077L & 0xffffffffL)),
            ((343642656L << 32) | (-1405984346L & 0xffffffffL)),
            ((14960981L << 32) | (-1790368967L & 0xffffffffL)),
            ((238480842L << 32) | (-753375757L & 0xffffffffL)),
            ((447297215L << 32) | (-362600596L & 0xffffffffL)),
            ((474204509L << 32) | (-1196688411L & 0xffffffffL)),
            ((148640296L << 32) | (-2117027462L & 0xffffffffL)),
            ((101627575L << 32) | (-947781712L & 0xffffffffL)),
            ((311520706L << 32) | (-17122001L & 0xffffffffL)),
            ((464256905L << 32) | (1260614180L & 0xffffffffL)),
            ((255075580L << 32) | (1918382267L & 0xffffffffL)),
            ((31883363L << 32) | (873035377L & 0xffffffffL)),
            ((360274710L << 32) | (225588462L & 0xffffffffL)),
            ((409403215L << 32) | (590454470L & 0xffffffffL)),
            ((217654330L << 32) | (441113689L & 0xffffffffL)),
            ((36405413L << 32) | (1545312915L & 0xffffffffL)),
            ((380918736L << 32) | (1700845580L & 0xffffffffL)),
            ((529451419L << 32) | (-793176313L & 0xffffffffL)),
            ((185668334L << 32) | (-373140072L & 0xffffffffL)),
            ((96712305L << 32) | (-1349315758L & 0xffffffffL)),
            ((291269892L << 32) | (-1763159603L & 0xffffffffL)),
            ((397841126L << 32) | (-1004499644L & 0xffffffffL)),
            ((53037459L << 32) | (-44314661L & 0xffffffffL)),
            ((234614028L << 32) | (-1156937455L & 0xffffffffL)),
            ((425997945L << 32) | (-2106471538L & 0xffffffffL)),
            ((274637874L << 32) | (933758085L & 0xffffffffL)),
            ((79789895L << 32) | (248727066L & 0xffffffffL)),
            ((169073624L << 32) | (1216479440L & 0xffffffffL)),
            ((512491693L << 32) | (1912160847L & 0xffffffffL)),
            ((130425884L << 32) | (-317474366L & 0xffffffffL)),
            ((324665193L << 32) | (-731317411L & 0xffffffffL)),
            ((495672310L << 32) | (-1841709673L & 0xffffffffL)),
            ((152338563L << 32) | (-1421672696L & 0xffffffffL)),
            ((3019464L << 32) | (513486851L & 0xffffffffL)),
            ((347195837L << 32) | (669020828L & 0xffffffffL)),
            ((442723618L << 32) | (1639003222L & 0xffffffffL)),
            ((251442775L << 32) | (1489663689L & 0xffffffffL)),
            ((135678389L << 32) | (167946816L & 0xffffffffL)),
            ((478778048L << 32) | (863627487L & 0xffffffffL)),
            ((307967583L << 32) | (1965600277L & 0xffffffffL)),
            ((113569066L << 32) | (1280568458L & 0xffffffffL)),
            ((268336993L << 32) | (-108387455L & 0xffffffffL)),
            ((459383828L << 32) | (-1057922786L & 0xffffffffL)),
            ((364052619L << 32) | (-2036324396L & 0xffffffffL)),
            ((19717118L << 32) | (-1076140725L & 0xffffffffL)),
            ((341147880L << 32) | (-1297526363L & 0xffffffffL)),
            ((13261725L << 32) | (-1949166790L & 0xffffffffL)),
            ((236519170L << 32) | (-847030800L & 0xffffffffL)),
            ((445064311L << 32) | (-185067665L & 0xffffffffL)),
            ((331200060L << 32) | (1092737124L & 0xffffffffL)),
            ((119696713L << 32) | (2019203835L & 0xffffffffL)),
            ((166775254L << 32) | (1040964657L & 0xffffffffL)),
            ((493818531L << 32) | (124821166L & 0xffffffffL)),
            ((461761857L << 32) | (1438138919L & 0xffffffffL)),
            ((253376052L << 32) | (1824719032L & 0xffffffffL)),
            ((29921963L << 32) | (714229362L & 0xffffffffL)),
            ((358042078L << 32) | (334038253L & 0xffffffffL)),
            ((476961685L << 32) | (-1506751514L & 0xffffffffL)),
            ((150077664L << 32) | (-1622439559L & 0xffffffffL)),
            ((102802559L << 32) | (-652554317L & 0xffffffffL)),
            ((314539786L << 32) | (-530477780L & 0xffffffffL)),
            ((200629179L << 32) | (2089514657L & 0xffffffffL)),
            ((527073486L << 32) | (1173369918L & 0xffffffffL)),
            ((297280593L << 32) | (60912372L & 0xffffffffL)),
            ((86507300L << 32) | (987377771L & 0xffffffffL)),
            ((203255151L << 32) | (-1895563424L & 0xffffffffL)),
            ((411219482L << 32) | (-1233601025L & 0xffffffffL)),
            ((374346373L << 32) | (-265684171L & 0xffffffffL)),
            ((47172080L << 32) | (-917325398L & 0xffffffffL)),
            ((69547538L << 32) | (-1684380381L & 0xffffffffL)),
            ((280685927L << 32) | (-1562302532L & 0xffffffffL)),
            ((510151160L << 32) | (-458202762L & 0xffffffffL)),
            ((183997069L << 32) | (-573889559L & 0xffffffffL)),
            ((63766726L << 32) | (1746070754L & 0xffffffffL)),
            ((391306163L << 32) | (1365880445L & 0xffffffffL)),
            ((427851564L << 32) | (389605559L & 0xffffffffL)),
            ((220177497L << 32) | (776186408L & 0xffffffffL)),
            ((62863262L << 32) | (1270856935L & 0xffffffffL)),
            ((388033771L << 32) | (1925512824L & 0xffffffffL)),
            ((435308660L << 32) | (882227378L & 0xffffffffL)),
            ((225284865L << 32) | (233765421L & 0xffffffffL)),
            ((72810826L << 32) | (-1204341466L & 0xffffffffL)),
            ((281598527L << 32) | (-2125695047L & 0xffffffffL)),
            ((505052832L << 32) | (-954388109L & 0xffffffffL)),
            ((176530901L << 32) | (-26840084L & 0xffffffffL)),
            ((208624183L << 32) | (-1397837979L & 0xffffffffL)),
            ((418414914L << 32) | (-1781142022L & 0xffffffffL)),
            ((371336669L << 32) | (-746280144L & 0xffffffffL)),
            ((46005928L << 32) | (-352327249L & 0xffffffffL)),
            ((193424611L << 32) | (1596335780L & 0xffffffffL)),
            ((521713558L << 32) | (1717396539L & 0xffffffffL)),
            ((298455817L << 32) | (537666289L & 0xffffffffL)),
            ((89507964L << 32) | (427253870L & 0xffffffffL)),
            ((471854285L << 32) | (-2053268509L & 0xffffffffL)),
            ((142620600L << 32) | (-1125721732L & 0xffffffffL)),
            ((106074919L << 32) | (-88629322L & 0xffffffffL)),
            ((315443282L << 32) | (-1009984215L & 0xffffffffL)),
            ((469228057L << 32) | (1981092386L & 0xffffffffL)),
            ((258474348L << 32) | (1332629693L & 0xffffffffL)),
            ((29009395L << 32) | (155129463L & 0xffffffffL)),
            ((354778758L << 32) | (809784552L & 0xffffffffL)),
            ((332366180L << 32) | (1653377121L & 0xffffffffL)),
            ((122706449L << 32) | (1542966014L & 0xffffffffL)),
            ((159579790L << 32) | (497454132L & 0xffffffffL)),
            ((488449531L << 32) | (618516139L & 0xffffffffL)),
            ((338147248L << 32) | (-1862008416L & 0xffffffffL)),
            ((12086469L << 32) | (-1468054721L & 0xffffffffL)),
            ((241879130L << 32) | (-298973707L & 0xffffffffL)),
            ((452268847L << 32) | (-682277014L & 0xffffffffL)),
            ((260851769L << 32) | (-634948732L & 0xffffffffL)),
            ((454267724L << 32) | (-480497381L & 0xffffffffL)),
            ((364984275L << 32) | (-1525844015L & 0xffffffffL)),
            ((22998182L << 32) | (-1669974706L & 0xffffffffL)),
            ((140803821L << 32) | (699398725L & 0xffffffffL)),
            ((486253976L << 32) | (282376410L & 0xffffffffL)),
            ((304677127L << 32) | (1451621904L & 0xffffffffL)),
            ((112646770L << 32) | (1878965391L & 0xffffffffL)),
            ((6038928L << 32) | (1026973702L & 0xffffffffL)),
            ((348389093L << 32) | (72163993L & 0xffffffffL)),
            ((437344890L << 32) | (1109156947L & 0xffffffffL)),
            ((244220175L << 32) | (2070357708L & 0xffffffffL)),
            ((129241924L << 32) | (-826349113L & 0xffffffffL)),
            ((321636401L << 32) | (-138040488L & 0xffffffffL)),
            ((502885550L << 32) | (-1315639918L & 0xffffffffL)),
            ((157726683L << 32) | (-1997558003L & 0xffffffffL)),
            ((271356778L << 32) | (335893632L & 0xffffffffL)),
            ((78858271L << 32) | (763237919L & 0xffffffffL)),
            ((174189696L << 32) | (1798262997L & 0xffffffffL)),
            ((519976949L << 32) | (1381241418L & 0xffffffffL)),
            ((398763454L << 32) | (-410133183L & 0xffffffffL)),
            ((56327883L << 32) | (-554262562L & 0xffffffffL)),
            ((227138132L << 32) | (-1733830380L & 0xffffffffL)),
            ((420872481L << 32) | (-1579377781L & 0xffffffffL)),
            ((536673987L << 32) | (-216774910L & 0xffffffffL)),
            ((191047094L << 32) | (-898693731L & 0xffffffffL)),
            ((95519017L << 32) | (-1942076585L & 0xffffffffL)),
            ((288250460L << 32) | (-1253768760L & 0xffffffffL)),
            ((404015127L << 32) | (10276547L & 0xffffffffL)),
            ((210441058L << 32) | (971476060L & 0xffffffffL)),
            ((39434237L << 32) | (2142685846L & 0xffffffffL)),
            ((382102664L << 32) | (1187874825L & 0xffffffffL)),
            ((467257553L << 32) | (1754145313L & 0xffffffffL)),
            ((256250788L << 32) | (1375035582L & 0xffffffffL)),
            ((26523451L << 32) | (396633716L & 0xffffffffL)),
            ((353070158L << 32) | (786392299L & 0xffffffffL)),
            ((473038341L << 32) | (-1694061600L & 0xffffffffL)),
            ((145630576L << 32) | (-1568806529L & 0xffffffffL)),
            ((108823023L << 32) | (-466833483L & 0xffffffffL)),
            ((316889754L << 32) | (-581440214L & 0xffffffffL)),
            ((336176504L << 32) | (-1885392477L & 0xffffffffL)),
            ((9862669L << 32) | (-1226542276L & 0xffffffffL)),
            ((239393426L << 32) | (-256559626L & 0xffffffffL)),
            ((450560487L << 32) | (-909215895L & 0xffffffffL)),
            ((333550508L << 32) | (2081929314L & 0xffffffffL)),
            ((125716697L << 32) | (1164770045L & 0xffffffffL)),
            ((162327622L << 32) | (54377527L & 0xffffffffL)),
            ((489895731L << 32) | (977731240L & 0xffffffffL)),
            ((75043714L << 32) | (-1498675931L & 0xffffffffL)),
            ((283560183L << 32) | (-1613281350L & 0xffffffffL)),
            ((506752104L << 32) | (-645529232L & 0xffffffffL)),
            ((179025693L << 32) | (-520272913L & 0xffffffffL)),
            ((59843926L << 32) | (1428458724L & 0xffffffffL)),
            ((386858531L << 32) | (1818218107L & 0xffffffffL)),
            ((433871548L << 32) | (705595569L & 0xffffffffL)),
            ((222527945L << 32) | (326486574L & 0xffffffffL)),
            ((195657259L << 32) | (1102911143L & 0xffffffffL)),
            ((523674974L << 32) | (2026263608L & 0xffffffffL)),
            ((300155329L << 32) | (1050088178L & 0xffffffffL)),
            ((92002996L << 32) | (132927597L & 0xffffffffL)),
            ((205605119L << 32) | (-1305108634L & 0xffffffffL)),
            ((417239946L << 32) | (-1957765639L & 0xffffffffL)),
            ((369899285L << 32) | (-853566669L & 0xffffffffL)),
            ((43248736L << 32) | (-194717268L & 0xffffffffL)),
            ((401258358L << 32) | (-115937982L & 0xffffffffL)),
            ((58027011L << 32) | (-1066553379L & 0xffffffffL)),
            ((229099676L << 32) | (-2042828521L & 0xffffffffL)),
            ((423105513L << 32) | (-1085822072L & 0xffffffffL)),
            ((268599714L << 32) | (178152579L & 0xffffffffL)),
            ((77421271L << 32) | (870655516L & 0xffffffffL)),
            ((173014600L << 32) | (1974755542L & 0xffffffffL)),
            ((516957501L << 32) | (1288643145L & 0xffffffffL)),
            ((406510303L << 32) | (503840448L & 0xffffffffL)),
            ((212140458L << 32) | (662486111L & 0xffffffffL)),
            ((41395509L << 32) | (1630403221L & 0xffffffffL)),
            ((384335424L << 32) | (1482078218L & 0xffffffffL)),
            ((533916683L << 32) | (-309364991L & 0xffffffffL)),
            ((189609854L << 32) | (-722192994L & 0xffffffffL)),
            ((94344161L << 32) | (-1834650796L & 0xffffffffL)),
            ((285231252L << 32) | (-1411501621L & 0xffffffffL)),
            ((139095077L << 32) | (926206534L & 0xffffffffL)),
            ((483768144L << 32) | (240093401L & 0xffffffffL)),
            ((302453711L << 32) | (1209978387L & 0xffffffffL)),
            ((110676154L << 32) | (1902480524L & 0xffffffffL)),
            ((262298353L << 32) | (-994294905L & 0xffffffffL)),
            ((457015684L << 32) | (-37289704L & 0xffffffffL)),
            ((367994139L << 32) | (-1147779118L & 0xffffffffL)),
            ((24182382L << 32) | (-2098395827L & 0xffffffffL)),
            ((127533452L << 32) | (-802825788L & 0xffffffffL)),
            ((319150841L << 32) | (-379675813L & 0xffffffffL)),
            ((500661862L << 32) | (-1357914735L & 0xffffffffL)),
            ((155755795L << 32) | (-1770742002L & 0xffffffffL)),
            ((7485272L << 32) | (598560773L & 0xffffffffL)),
            ((351136813L << 32) | (450237082L & 0xffffffffL)),
            ((440354994L << 32) | (1552372816L & 0xffffffffL)),
            ((245404615L << 32) | (1711019727L & 0xffffffffL))
    };
    public static long EmptyFingerprint = PolynomialOne;

    /**
     * Take the first n parts of a string array and hash them.
     *
     * @param parts
     * @param depth
     * @return hashcde
     */
    public static long hash(String parts[], int depth) {
        long hash = FPHash64.getFP(parts[0]);
        for (int i = 1; i < depth; i++) {
            hash = FPHash64.combineFingerPrints(hash, FPHash64.getFP(parts[i]));
        }
        return hash;
    }

    /**
     * Return a combination of 2 fingerprints - i.e. a fingerprint that would result from a combination of the two texts
     * that returned the corresponding fingerprints. Allocates 2 arrays. Use the other version if you don't want
     * allocation to occur
     *
     * @param fp1 Fingerprint1
     * @param fp2 Fingerprint2
     * @return The combined fingerprint
     */
    public static final long combineFingerPrints(long fp1, long fp2) {
        // create an array of bytes with fingerprints in little-endian format
        byte buf16[] = new byte[16];
        int buf8[] = new int[8];
        return combineFingerPrints(fp1, fp2, buf16, buf8);
    }

    /**
     * Return a combination of 2 fingerprints - i.e. a fingerprint that would result from a combination of the two texts
     * that returned the corresponding fingerprints. This version doesn't do any memory allocation, since the user
     * provides the memory buffers
     *
     * @param fp1   Fingerprint1
     * @param fp2   Fingerprint2
     * @param buf16 byte array buffer of size 16
     * @param buf8  int array buffer of size 8
     * @return The combined fingerprint
     */

    public static final long combineFingerPrints(long fp1, long fp2, byte buf16[], int buf8[]) {
        // create an array of bytes with fingerprints in little-endian format
        polyToBytes(fp1, buf16, 0);
        polyToBytes(fp2, buf16, 8);

        long poly1 = polynomialComputeModule(PolynomialOne, buf16, 0, 16);

        int p0 = (int) (poly1 & 0xffffffffL);
        int p1 = (int) ((poly1 >>> 32) & 0xffffffffL);
        int t0 = ((p0 * FINGERPRINT_A) + (p1 * FINGERPRINT_B));
        int t1 = ((p0 * FINGERPRINT_C) + (p1 * FINGERPRINT_D));

        long poly2 = (((long) t1) << 32) | (((long) t0) & 0xffffffffL);
        poly_to_bytes(poly2, buf8, 0);

        for (int i = 0; i < 8; ++i) {
            buf8[i] = fingerprint_perm[buf8[i]];
        }

        long result = poly_from_bytes(buf8);
        return result;
    }

    /**
     * Compare a fingerprint with some bytes
     *
     * @param fp    - the fingerprint to compare
     * @param bytes - the bytes to compare against
     * @return true if they are equal
     */
    public static boolean compareFingerprint(long fp, int bytes[]) {
        // Assume the bytes are in little-endian order.
        int t0 = bytes[0] | (bytes[1] << 8) | (bytes[2] << 16) | (bytes[3] << 24);
        int t1 = bytes[4] | (bytes[5] << 8) | (bytes[6] << 16) | (bytes[7] << 24);
        long fp1 = (((long) t1) << 32) | (((long) t0) & 0xffffffffL);
        return fp == fp1;
    }

    /**
     * Check if the two given fingerprints are equal
     *
     * @param fp1 - fingerprint 1
     * @param fp2 - fingerprint 2
     * @return true if equal
     */
    public static boolean equals(long fp1, long fp2) {
        return fp1 == fp2;
    }


    private static final int extractWord(int x, int i, int n) {
        return ((x >>> i) & ~(-1 << n));
    }

    public static final char[] getChars(String s) {
        int length = s.length();
        char c[] = new char[length];
        int srcBegin = 0;
        int destBegin = 0;
        s.getChars(srcBegin, srcBegin + length, c, destBegin);
        return c;
    }

    /**
     * Return a 64 bit (Java long) hash/fingerprint/message-digest of the given text using Andrei Broder's Modula-3
     * fingerprint algorithm
     *
     * @param text The bytes that need to be hashed
     * @return the 64bit message digest as a java primitive (long)
     */

    public static final long getFingerprint(byte[] text) {
        return polynomialComputeModule(PolynomialOne, text, 0, text.length);
    }

    /**
     * Return a 64 bit (Java long) hash/fingerprint/message-digest of the given text using Andrei Broder's Modula-3
     * fingerprint algorithm
     *
     * @param text  The bytes that need to be hashed
     * @param begin The index into Text where to begin hashing
     * @param len   The length of the bytes to be hashed (starting from begin)
     * @return the 64bit message digest as a java primitive (long)
     */
    public static final long getFingerprint(byte[] text, int begin, int len) {
        long result = polynomialComputeModule(PolynomialOne, text, begin, len);
        return result;
    }

    /**
     * Return a 64 bit (Java long) hash/fingerprint/message-digest of the given text using Andrei Broder's Modula-3
     * fingerprint algorithm
     *
     * @param text The bytes that need to be hashed
     * @return the 64bit message digest as a java primitive (long)
     */

    public static final long getFingerprint(char[] text) {
        return polynomialComputeModuleCharNew(PolynomialOne, text, 0, text.length);
    }

    public static final long getFingerprint(char[] text, int start, int length) {
        return polynomialComputeModuleCharNew(PolynomialOne, text, start, length);
    }

    public static final long getFingerprint(char[] text, int length) {
        return polynomialComputeModuleCharNew(PolynomialOne, text, 0, length);
    }

    /**
     * Return a 64 bit (Java long) hash/fingerprint/message-digest of the given text using Andrei Broder's Modula-3
     * fingerprint algorithm
     *
     * @param text The string that need to be hashed (using UTF-8 char encoding)
     * @return the 64bit message digest as a java primitive (long)
     */

    public static final long getFP(String text) {
        if (text == null) {
            return 0;
        }

        try {
            return getFingerprint(text.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            Log.util.error("Error %s %e", e, e);
            return 0;
        }
    }

    public static final long getFP(String... text) {
        if (text == null) {
            return 0;
        }

        try {
            long hash = 0;
            for (String t : text) {
                if (t == null) {
                    continue;
                }
                hash = combineFingerPrints(hash, getFingerprint(t.getBytes("UTF-8")));
            }
            return hash;
        } catch (UnsupportedEncodingException e) {
            Log.util.error("Error %s %e", e, e);
            return 0;
        }
    }

    /**
     * Alternative function that does not utf-8 encode, uses chars instead
     *
     * @param text
     * @return
     */
    public static final long getFPViaChars(String text) {
        if (text == null) {
            return 0;
        }

        return getFingerprint(getChars(text));
    }

    /**
     * Create a integer hash of the fingerprint
     *
     * @param fp - fingerprint
     * @return an integer hash of fp
     */
    public static int hashFingerprint(long fp) {
        return (int) ((fp & 0xffffffff) ^ ((fp >>> 32) & 0xffffffff));
    }

    public static void main(String args[]) {
        String a1 = "abcd";
        String a2 = "efgh";
        String a3 = a1 + a2;
        char[] a = getChars(a1);
        char[] b = getChars(a2);
        char[] c = getChars(a3);

        long fp1 = FPHash64.getFP(a1);
        long fp2 = FPHash64.getFP(a2);
        long fp3 = FPHash64.getFP(a3);
        long fpcomb = FPHash64.combineFingerPrints(fp1, fp2);

        long fp1p = FPHash64.getFingerprint(a);
        long fp2p = FPHash64.getFingerprint(b);
        long fp3p = FPHash64.getFingerprint(c);
        long fpcombprime = FPHash64.combineFingerPrints(fp1p, fp2p);


    }

    //	create a poly from bytes that are in little-endian form
    private static final long poly_from_bytes(int[] b) {
        // Assume the bytes are in little-endian order.
        int t0 = b[0] | b[1] << 8 | b[2] << 16 | b[3] << 24;
        int t1 = b[4] | b[5] << 8 | b[6] << 16 | b[7] << 24;
        return (((long) t1) << 32) | (((long) t0) & 0xffffffffL);
    }

    // create a little-endian representation of the
    // bytes constituting T in B (starting at F).
    private static final void poly_to_bytes(long t, int[] b, int f) {
        // Generate the bytes in little-endian order.
        int p0 = (int) (t & 0xffffffffL);
        int p1 = (int) ((t >>> 32) & 0xffffffffL);
        b[f + 0] = extractWord(p0, 0, 8);
        b[f + 1] = extractWord(p0, 8, 8);
        b[f + 2] = extractWord(p0, 16, 8);
        b[f + 3] = extractWord(p0, 24, 8);
        b[f + 4] = extractWord(p1, 0, 8);
        b[f + 5] = extractWord(p1, 8, 8);
        b[f + 6] = extractWord(p1, 16, 8);
        b[f + 7] = extractWord(p1, 24, 8);
    }

    /**
     * This procedure assumes that the LEN bytes beginning at address ADDR define a polynomial, A(x) of getDegree 8 *
     * LEN.  The procedure returns (INIT * x ^ (8 * LEN) + A(x)) % PolyBasis.P.
     * <p/>
     * This is where everything happens. Heavily optimized for JIT-ted run-time performance and thus code modularity is
     * sacrificed.
     *
     * @return : the fingerprint as a long
     * @init : initial hash polynomial
     * @addr : the byte array to be hashed
     * @begin: beginning location
     * @len :  length of string in bytes
     */
    private static final long polynomialComputeModule(long init, byte[] addr, int begin, int len) {
        int j = 0, k = 0;
        long result = init;
        int a0 = 0, a1 = 0, a2 = 0, a3 = 0;
        int b0 = 0, b1 = 0, b2 = 0, b3 = 0;
        int shift8 = ~(-1 << 8);

        if (len >= 4) {
            j = len % 4;
            k = len - j;
            int len1 = k;
            int l = begin;
            // get the two words
            int p0 = (int) (result & 0xffffffff);
            int p1 = (int) ((result >>> 32) & 0xffffffff);

            while (len1 > 0) {
                // Split the low-order bytes to little-endian form.
                a0 = p0 & shift8;
                a1 = (p0 >>> 8) & shift8;
                a2 = (p0 >>> 16) & shift8;
                a3 = (p0 >>> 24) & shift8;

                long t1 = Polynomial88[a0];
                long t2 = Polynomial80[a1];
                long t3 = Polynomial72[a2];
                long t4 = Polynomial64[a3];

                p0 = p1 ^ (int) ((t1 & 0xffffffff) ^ (t2 & 0xffffffff) ^ (t3 & 0xffffffff) ^ (t4 & 0xffffffff));
                int ad1 = addr[l] & 0xff;
                int ad2 = addr[++l] & 0xff;
                int ad3 = addr[++l] & 0xff;
                int ad4 = addr[++l] & 0xff;
                int ip = ad1 | ad2 << 8 | ad3 << 16 | ad4 << 24;
                p1 = ip ^ (int) (((t1 >>> 32) & 0xffffffff) ^ ((t2 >>> 32) & 0xffffffff) ^ ((t3 >>> 32) & 0xffffffff) ^ ((t4 >>> 32) & 0xffffffff));
                len1 -= 4;
                ++l;
            }

            result = (((long) p1) << 32) | (((long) p0) & 0xffffffffL);
            len = j;
        }

        if (len > 0) {
            int n_bits = 8 * len;
            int x_bits = 32 - n_bits;
            int t0 = (int) (result & 0xffffffff);
            int t0_x = t0 << x_bits;
            int t0_n = t0 >>> n_bits;
            int t1 = (int) ((result >>> 32) & 0xffffffff);
            int t1_x = t1 << x_bits;
            int t1_n = t1 >>> n_bits;

            long result1 = (((long) (t0_n ^ t1_x)) << 32) | (((long) t0_x) & 0xffffffffL);

            switch (len) {
                case 1:
                    a0 = (t1_n & shift8);
                    a1 = ((t1_n >>> 8) & shift8);
                    a2 = ((t1_n >>> 16) & shift8);
                    a3 = addr[k + 0] & 0xff;
                    break;
                case 2:
                    a0 = (t1_n & shift8);
                    a1 = ((t1_n >>> 8) & shift8);
                    a2 = addr[k + 0] & 0xff;
                    a3 = addr[k + 1] & 0xff;
                    break;
                case 3:
                    a0 = (t1_n & shift8);
                    a1 = addr[k + 0] & 0xff;
                    a2 = addr[k + 1] & 0xff;
                    a3 = addr[k + 2] & 0xff;
                    break;
                default:
                    break;
            }

            int len_x = 4;
            int p0 = (int) (result1 & 0xffffffff);
            int p1 = (int) ((result1 >>> 32) & 0xffffffff);
            int l = 0;

            while (len_x > 0) {
                // Split the low-order bytes to little-endian form.
                b0 = p0 & shift8;
                b1 = (p0 >>> 8) & shift8;
                b2 = (p0 >>> 16) & shift8;
                b3 = (p0 >>> 24) & shift8;
                long ty0 = Polynomial88[b0];
                long ty1 = Polynomial80[b1];
                long ty2 = Polynomial72[b2];
                long ty3 = Polynomial64[b3];
                // Compute the new result.
                p0 = p1 ^ (int) ((ty0 & 0xffffffff) ^ (ty1 & 0xffffffff) ^ (ty2 & 0xffffffff) ^ (ty3 & 0xffffffff));
                int ip = a0 | (a1 << 8) | (a2 << 16) | (a3 << 24);
                p1 = ip ^ (int) (((ty0 >>> 32) & 0xffffffff) ^ ((ty1 >>> 32) & 0xffffffff) ^ ((ty2 >>> 32) & 0xffffffff) ^ ((ty3 >>> 32) & 0xffffffff));
                len_x -= 4;
                l += 4;
            }
            result = (((long) p1) << 32) | (((long) p0) & 0xffffffffL);
        }

        return result;
    }

    private static final long polynomialComputeModuleCharNew(long init, char[] addr, int begin, int len) {
        int j = 0, k = 0;
        long result = init;
        int a0 = 0, a1 = 0, a2 = 0, a3 = 0;
        int b0 = 0, b1 = 0, b2 = 0, b3 = 0;
        int shift8 = ~(-1 << 8);

        if (len >= 4) {
            j = len % 4;
            k = len - j;
            int len1 = k;
            int l = begin;
            // get the two words
            int p0 = (int) (result & 0xffffffff);
            int p1 = (int) ((result >>> 32) & 0xffffffff);

            while (len1 > 0) {
                // Split the low-order bytes to little-endian form.
                a0 = p0 & shift8;
                a1 = (p0 >>> 8) & shift8;
                a2 = (p0 >>> 16) & shift8;
                a3 = (p0 >>> 24) & shift8;

                long t1 = Polynomial88[a0];
                long t2 = Polynomial80[a1];
                long t3 = Polynomial72[a2];
                long t4 = Polynomial64[a3];

                p0 = p1 ^ (int) ((t1 & 0xffffffff) ^ (t2 & 0xffffffff) ^ (t3 & 0xffffffff) ^ (t4 & 0xffffffff));
                int ad1 = addr[l] & 0xffff;
                int ad2 = addr[++l] & 0xffff;
                int ad3 = addr[++l] & 0xffff;
                int ad4 = addr[++l] & 0xffff;
                int ip = ad1 | ad2 << 8 | ad3 << 16 | ad4 << 24;
                p1 = ip ^ (int) (((t1 >>> 32) & 0xffffffff) ^ ((t2 >>> 32) & 0xffffffff) ^ ((t3 >>> 32) & 0xffffffff) ^ ((t4 >>> 32) & 0xffffffff));
                len1 -= 4;
                ++l;
            }

            result = (((long) p1) << 32) | (((long) p0) & 0xffffffffL);
            len = j;
        }

        if (len > 0) {
            int n_bits = 8 * len;
            int x_bits = 32 - n_bits;
            int t0 = (int) (result & 0xffffffff);
            int t0_x = t0 << x_bits;
            int t0_n = t0 >>> n_bits;
            int t1 = (int) ((result >>> 32) & 0xffffffff);
            int t1_x = t1 << x_bits;
            int t1_n = t1 >>> n_bits;

            long result1 = (((long) (t0_n ^ t1_x)) << 32) | (((long) t0_x) & 0xffffffffL);

            switch (len) {
                case 1:
                    a0 = (t1_n & shift8);
                    a1 = ((t1_n >>> 8) & shift8);
                    a2 = ((t1_n >>> 16) & shift8);
                    a3 = addr[k + 0] & 0xffff;
                    break;
                case 2:
                    a0 = (t1_n & shift8);
                    a1 = ((t1_n >>> 8) & shift8);
                    a2 = addr[k + 0] & 0xffff;
                    a3 = addr[k + 1] & 0xffff;
                    break;
                case 3:
                    a0 = (t1_n & shift8);
                    a1 = addr[k + 0] & 0xffff;
                    a2 = addr[k + 1] & 0xffff;
                    a3 = addr[k + 2] & 0xffff;
                    break;
                default:
                    break;
            }

            int len_x = 4;
            int p0 = (int) (result1 & 0xffffffff);
            int p1 = (int) ((result1 >>> 32) & 0xffffffff);
            int l = 0;

            while (len_x > 0) {
                // Split the low-order bytes to little-endian form.
                b0 = p0 & shift8;
                b1 = (p0 >>> 8) & shift8;
                b2 = (p0 >>> 16) & shift8;
                b3 = (p0 >>> 24) & shift8;
                long ty0 = Polynomial88[b0];
                long ty1 = Polynomial80[b1];
                long ty2 = Polynomial72[b2];
                long ty3 = Polynomial64[b3];
                // Compute the new result.
                p0 = p1 ^ (int) ((ty0 & 0xffffffff) ^ (ty1 & 0xffffffff) ^ (ty2 & 0xffffffff) ^ (ty3 & 0xffffffff));
                int ip = a0 | (a1 << 8) | (a2 << 16) | (a3 << 24);
                p1 = ip ^ (int) (((ty0 >>> 32) & 0xffffffff) ^ ((ty1 >>> 32) & 0xffffffff) ^ ((ty2 >>> 32) & 0xffffffff) ^ ((ty3 >>> 32) & 0xffffffff));
                len_x -= 4;
                l += 4;
            }
            result = (((long) p1) << 32) | (((long) p0) & 0xffffffffL);
        }

        return result;
    }

    /**
     * This procedure assumes that the LEN bytes beginning at address ADDR define a polynomial, A(x) of getDegree 8 *
     * LEN.  The procedure returns (INIT * x ^ (8 * LEN) + A(x)) % PolyBasis.P.
     * <p/>
     * This is where everything happens. Heavily optimized for JIT-ted run-time performance and thus code modularity is
     * sacrificed.
     *
     * @return : the fingerprint as a long
     * @init : initial hash polynomial
     * @addr : the byte array to be hashed
     * @begin: beginning location
     * @len :  length of string in bytes
     */
    private static final long polynomialComputeModuleCharOLDFUCKED(long init, char[] addr, int begin, int len) {
        int j = 0, k = 0;
        long result = init;
        int a0 = 0, a1 = 0, a2 = 0, a3 = 0;
        int b0 = 0, b1 = 0, b2 = 0, b3 = 0;
        int shift8 = ~(-1 << 8);

        if (len >= 2) {
            j = len % 2;
            k = len - j;
            int len1 = k;
            int l = begin;
            // get the two words
            int p0 = (int) (result & 0xffffffff);
            int p1 = (int) ((result >>> 32) & 0xffffffff);

            while (len1 > 0) {
                // Split the low-order bytes to little-endian form.
                a0 = p0 & shift8;
                a1 = (p0 >>> 8) & shift8;
                a2 = (p0 >>> 16) & shift8;
                a3 = (p0 >>> 24) & shift8;
                long t1 = Polynomial88[a0];
                long t2 = Polynomial80[a1];
                long t3 = Polynomial72[a2];
                long t4 = Polynomial64[a3];

                p0 = p1 ^ (int) ((t1 & 0xffffffff) ^ (t2 & 0xffffffff) ^ (t3 & 0xffffffff) ^ (t4 & 0xffffffff));
                // upper and lower part of the char
                char a = addr[l++];
                char b = addr[l++];
                int ad1 = a & 0xff;
                int ad2 = a & 0xff00;
                int ad3 = b & 0xff;
                int ad4 = b & 0xff00;
                int ip = ad1 | ad2 | ad3 << 16 | ad4 << 16;
                p1 = ip ^ (int) (((t1 >>> 32) & 0xffffffff) ^ ((t2 >>> 32) & 0xffffffff) ^ ((t3 >>> 32) & 0xffffffff) ^ ((t4 >>> 32) & 0xffffffff));
                len1 -= 4;
                ++l;
            }

            result = (((long) p1) << 32) | (((long) p0) & 0xffffffffL);
            len = j;
        }

        if (len > 0) {
            int n_bits = 16 * len;
            int x_bits = 32 - n_bits;
            int t0 = (int) (result & 0xffffffff);
            int t0_x = t0 << x_bits;
            int t0_n = t0 >>> n_bits;
            int t1 = (int) ((result >>> 32) & 0xffffffff);
            int t1_x = t1 << x_bits;
            int t1_n = t1 >>> n_bits;

            long result1 = (((long) (t0_n ^ t1_x)) << 32) | (((long) t0_x) & 0xffffffffL);

            switch (len) {
                case 1:
                    a0 = (t1_n & shift8);
                    a1 = ((t1_n >>> 8) & shift8);
                    a2 = ((t1_n >>> 16) & shift8);
                    char a = addr[k];
                    a2 = a & 0xff;
                    a3 = a & 0xff00;
                    break;
                case 2:
                    // wrong
                    break;
                case 3:
                    //wrong
                    break;
                default:
                    break;
            }

            int len_x = 4;
            int p0 = (int) (result1 & 0xffffffff);
            int p1 = (int) ((result1 >>> 32) & 0xffffffff);
            int l = 0;

            while (len_x > 0) {
                // Split the low-order bytes to little-endian form.
                b0 = p0 & shift8;
                b1 = (p0 >>> 8) & shift8;
                b2 = (p0 >>> 16) & shift8;
                b3 = (p0 >>> 24) & shift8;
                long ty0 = Polynomial88[b0];
                long ty1 = Polynomial80[b1];
                long ty2 = Polynomial72[b2];
                long ty3 = Polynomial64[b3];
                // Compute the new result.
                p0 = p1 ^ (int) ((ty0 & 0xffffffff) ^ (ty1 & 0xffffffff) ^ (ty2 & 0xffffffff) ^ (ty3 & 0xffffffff));
                int ip = a0 | (a1 << 8) | (a2 << 16) | (a3 << 24);
                p1 = ip ^ (int) (((ty0 >>> 32) & 0xffffffff) ^ ((ty1 >>> 32) & 0xffffffff) ^ ((ty2 >>> 32) & 0xffffffff) ^ ((ty3 >>> 32) & 0xffffffff));
                len_x -= 4;
                l += 4;
            }
            result = (((long) p1) << 32) | (((long) p0) & 0xffffffffL);
        }

        return result;
    }


    // create a little-endian representation of the
    // bytes constituting T in B (starting at F).
    private static final void polyToBytes(long t, byte[] b, int f) {
        // Generate the bytes in little-endian order.
        int p0 = (int) (t & 0xffffffffL);
        int p1 = (int) ((t >>> 32) & 0xffffffffL);
        b[f + 0] = (byte) extractWord(p0, 0, 8);
        b[f + 1] = (byte) extractWord(p0, 8, 8);
        b[f + 2] = (byte) extractWord(p0, 16, 8);
        b[f + 3] = (byte) extractWord(p0, 24, 8);
        b[f + 4] = (byte) extractWord(p1, 0, 8);
        b[f + 5] = (byte) extractWord(p1, 8, 8);
        b[f + 6] = (byte) extractWord(p1, 16, 8);
        b[f + 7] = (byte) extractWord(p1, 24, 8);
    }
}
